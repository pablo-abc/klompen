(ns klompen.html
  (:require
   [klompen.cache :refer [bindings]]
   [goog.object :as gobj]))

(defn- define-property-binding! [el key value host]
  (gobj/set el key (value host))
  (swap!
   (.get bindings host)
   conj
   #(gobj/set el key (value host))))

(defn- define-attribute-binding! [el key value host]
  (swap!
   (.get bindings host)
   conj
   #(.setAttribute el key (value host))))

(defn- define-boolean-attribute-binding! [el key value host]
  (swap!
   (.get bindings host)
   conj
   #(if (value host)
      (.setAttribute el key "")
      (.removeAttribute el key))))

(defn- assign-attributes!
  [el f r root]
  (let [host (or (.-host root) root)
        key (name (first f))
        modifier (namespace (first f))
        value (last f)]
    (cond
      (= modifier "on") (.addEventListener el key (fn [event] (value host event)))
      (and (= modifier "bool") (fn? value)) (define-boolean-attribute-binding! el key value host)
      (and (= modifier "prop") (fn? value)) (define-property-binding! el key value host)
      (= modifier "bool") (if value (.setAttribute el key "") (.removeAttribute el key))
      (= modifier "prop") (gobj/set el key value)
      (fn? value) (define-attribute-binding! el key value host)
      :else (.setAttribute el key value)))
  (when (seq r) (recur el (first r) (rest r) root)))

(defn render!
  "Renders hiccup to the provided HTML node or shadow root"
  ([root v]
   (let [fragment (js/document.createDocumentFragment)]
     (render! fragment (first v) (rest v) root)
     (.replaceChildren root fragment)))
  ([p f r root]
   (cond
     (keyword? f) (let [el (js/document.createElement (name f))]
                    (.appendChild p el)
                    (recur el (first r) (rest r) root))
     (fn? f) (let [host (or (.-host root) root)
                   fragment (js/document.createDocumentFragment)]
               (swap!
                (.get bindings host)
                conj
                #(do
                   (render! fragment (f host) r root)
                   (.replaceChildren p fragment))))
     (map? f) (do
                (assign-attributes! p (first f) (rest f) root)
                (recur p (first r) (rest r) root))
     (string? f) (let [node (js/document.createTextNode f)]
                   (.appendChild p node)
                   (recur p (first r) (rest r) root))
     (vector? f) (do (render! p (first f) (rest f) root)
                     (recur p (first r) (rest r) root))
     :else root)))
