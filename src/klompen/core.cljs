(ns klompen.core
  (:require
   [klompen.styles :refer [adopt-styles!]]
   [klompen.signal :refer [create-signal]]
   [klompen.html :refer [render! templates]]
   [clojure.string :refer [lower-case]]
   [goog.object :as gobj]))

(def ^:private properties (atom {}))

(defn ^:export add-method!
  "Assigns a method to a class' prototype"
  [c callback-key callback]
  (let [proto (.-prototype c)
        super-cb (gobj/get proto (name callback-key))]
    (js/Object.defineProperty
     proto
     (name callback-key)
     #js {:configurable true
          :value
          #(this-as
            this
            (when (fn? super-cb) (.call super-cb this))
            (callback this))})
    c))

(defn ^:export observe-attributes!
  "Assigns `observedAttributes` to the custom element"
  [c attributes cb]
  (js/Object.defineProperty
   c
   "observedAttributes"
   #js {:configurable true
        :value (.concat (clj->js attributes) (or (.-observedAttributes c) #js []))})
  (let [proto (.-prototype c)
        super-cb (.-attributeChangedCallback proto)]
    (js/Object.defineProperty
     proto
     "attributeChangedCallback"
     #js {:configurable true
          :value
          #(this-as
            this
            (when (fn? super-cb) (.call super-cb this %1 %2 %3))
            (cb this %1 %2 %3))}))
  c)

(defn ^:export add-observed-attribute!
  ([c attribute] (add-observed-attribute! c attribute identity))
  ([c attribute cb]
   (js/Object.defineProperty
    c
    "observedAttributes"
    #js {:configurable true
         :value (.concat  #js [attribute] (or (.-observedAttributes c) #js []))})
   (let [proto (.-prototype c)
         super-cb (.-attributeChangedCallback proto)]
     (js/Object.defineProperty
      (.-prototype c)
      "attributeChangedCallback"
      #js {:configurable true
           :value
           #(this-as
             this
             (when (fn? super-cb) (.call super-cb this %1 %2 %3))
             (cb this %1 %2 %3))}))
   c))

(defn ^:export add-property!
  "Assigns a reactive property to the element"
  ([c property value] (add-property! c
                                     property
                                     value {}))
  ([c property value opts]
   (let [attribute (if (some? (:attribute opts)) (:attribute opts) (lower-case property))
         type (or (:type opts) js/String)]
     (swap! properties assoc-in [c (keyword property)] {:value value
                                                        :type type
                                                        :attribute attribute})
     (when attribute
       (add-observed-attribute!
        c
        property
        #(when
          (= %2 attribute)
           (gobj/set %1 property %4))))
     c)))

(defn ^:export connect!
  "Assigns connectedCallback to element"
  [c cb]
  (add-method! c :connectedCallback cb))

(defn ^:export disconnect!
  "Assigns disconnectedCallback to element"
  [c cb]
  (add-method! c :disconnectedCallback cb))

(defn ^:export define!
  "Registers as custom element"
  [c element-name]
  (when (not (js/window.customElements.get element-name))
    (js/window.customElements.define element-name c))
  c)

(defn- setup-property! [el property value setter]
  (let [[v set-v] (create-signal value)]
    (js/Object.defineProperty
     el
     (name property)
     #js {:configurable true
          :get (fn []
                 (v))
          :set #(do
                  (set-v (setter % el)))})))

(defn ^:export create-ce
  "Creates constructor/class for a custom element.
   
   Example:
   ```
   (-> (create-ce)
       (set-html! [:h1 \"A title\"])
       (define! \"my-element\"))"
  ([] (create-ce #(.attachShadow % #js {:mode "open"})))
  ([cb]
   (let [constructor
         (fn const []
           (let [el
                 (js/Reflect.construct js/HTMLElement #js [] const)
                 styles (gobj/get const "styles")]
             (mapv
              #(setup-property! el (get % 0) (:value (get % 1)) (:type (get % 1)))
              (get @properties const))
             (.call cb el el)
             (render! (or (.-shadowRoot el) el) (get @templates const))
             (adopt-styles! (or (.-shadowRoot el) el) styles)
             el))]
     (set!
      (.-prototype constructor)
      (js/Object.create
       (.-prototype js/HTMLElement)))
     constructor)))