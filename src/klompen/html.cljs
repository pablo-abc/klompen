(ns klompen.html
  (:require
   [klompen.signal :refer [create-effect]]
   [goog.object :as gobj]))

(def templates (atom {}))

(defn- define-property-binding! [el key value host]
  (create-effect
   #(gobj/set el key (value host))))

(defn- define-attribute-binding! [el key value host]
  (create-effect #(.setAttribute el key (value host))))

(defn- define-boolean-attribute-binding! [el key value host]
  (create-effect
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

(defn set-html!
  "Sets html content of the custom element
   
   Example:
   ```
   (-> (create-ce)
       (set-html! [:div \"Hello world!\"]))
   ```"
  [c hc]
  (swap! templates assoc c hc)
  c)

(defn- get-tag-name
  [key]
  (re-find #"^[^.|^#]*" (name key)))

(defn- get-classes
  [key]
  (re-seq #"\.[^.|^#]*" (name key)))

(defn- get-id
  [key]
  (re-find #"#[^.|^#]*" (name key)))

(defn render!
  "Renders hiccup to the provided HTML node or shadow root
   
   Multiple calls to the same node replace all its children"
  ([root v]
   (let [fragment (js/document.createDocumentFragment)]
     (render! fragment (first v) (rest v) root)
     (.replaceChildren root fragment)))
  ([p f r root]
   (cond
     (keyword? f) (let [el (js/document.createElement (get-tag-name f))
                        classes (get-classes f)
                        id (get-id f)]
                    (when (some? id) (.setAttribute el "id" (subs id 1)))
                    (when (some? classes) (mapv #(.add el.classList (subs % 1)) classes))
                    (.appendChild p el)
                    (recur el (first r) (rest r) root))
     (fn? f) (let [host (or (.-host root) root)
                   fragment (js/document.createDocumentFragment)]
               (create-effect
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