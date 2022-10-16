(ns klompen.core
  (:require
   [klompen.styles :refer [adopt-styles!]]
   [klompen.signal :refer [create-signal create-effect]]
   [goog.object :as gobj]))

(def properties (atom {}))


(defn ^:export assign-method!
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
     (.-prototype c)
     "attributeChangedCallback"
     #js {:configurable true
          :value
          #(this-as
            this
            (when (fn? super-cb) (.call super-cb this %1 %2 %3))
            (cb this %1 %2 %3))}))
  c)

(defn ^:export add-observed-attribute!
  [c attribute]
  (js/Object.defineProperty
   c
   "observedAttributes"
   #js {:configurable true
        :value (.concat  #js [attribute] (or (.-observedAttributes c) #js []))})
  c)

(defn ^:export assign-property!
  "Assigns a reactive property to the element"
  ([c property value] (assign-property! c property value identity))
  ([c property value setter]
   (swap! properties assoc-in [c (keyword property)] {:value value
                                                      :type setter
                                                      :attribute property})
   (add-observed-attribute! c property)
   c))

(defn ^:export connect!
  "Assigns connectedCallback to element"
  [c cb]
  (assign-method! c :connectedCallback cb))

(defn ^:export disconnect!
  "Assigns disconnectedCallback to element"
  [c cb]
  (assign-method! c :disconnectedCallback cb))

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
  "Creates constructor/class for a custom element"
  ([] (create-ce identity))
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
             (adopt-styles! (or (.-shadowRoot el) el) styles)
             el))]
     (set!
      (.-prototype constructor)
      (js/Object.create
       (.-prototype js/HTMLElement)))
     (swap! properties assoc constructor {})
     constructor)))

(comment
  (->
   (create-ce)
   (assign-property! "test" false)
   (#(get @properties %))
   (->>
    (map #(print %))))
  (assign-property! (create-ce) "test" false)
  properties)