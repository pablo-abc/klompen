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

(defn- assign-attributes!
  [el f r root]
  (let [host (or (.-host root) root)
        key (name (first f))
        modifier (namespace (first f))
        value (last f)]
    (cond
      (= modifier "on") (.addEventListener el key (fn [event] (value host event)))
      (and (= modifier "prop") (fn? value)) (define-property-binding! el key value host)
      (= modifier "prop") (gobj/set el key value)
      (fn? value) (define-attribute-binding! el key value host)
      :else (.setAttribute el key value)))
  (when (seq r) (recur el (first r) (rest r) root)))

(defn render!
  "Renders hiccup to the provided HTML node (root)"
  ([root v]
   (render! root (first v) (rest v) root))
  ([p f r root]
   (cond
     (keyword? f) (let [el (js/document.createElement (name f))]
                    (.appendChild p el)
                    (recur el (first r) (rest r) root))
     (map? f) (do
                (assign-attributes! p (first f) (rest f) root)
                (recur p (first r) (rest r) root))
     (string? f) (let [node (js/document.createTextNode f)]
                   (.appendChild p node)
                   (recur p (first r) (rest r) root))
     (vector? f) (do (render! p (first f) (rest f) root)
                     (recur p (first r) (rest r) root))
     :else root)))
