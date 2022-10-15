(ns klompen.core
  (:require
   [klompen.cache :refer [cache bindings]]
   [goog.object :as gobj]))

(def default-values (js/Reflect.construct js/WeakMap #js []))

(defn- notify
  "Notify all bindings that values in the element have changed"
  [host]
  (mapv #(%) @(.get bindings host)))

(defn create-component
  "Creates constructor/class for a custom element"
  ([] (create-component identity))
  ([cb]
   (let [constructor
         (fn const []
           (let [el
                 (js/Reflect.construct js/HTMLElement #js [] const)]
             (.set cache el (atom {}))
             (.set bindings el (atom []))
             (.call cb el el)
             el))]
     (set!
      (.-prototype constructor)
      (js/Object.create
       (.-prototype js/HTMLElement)
       #js {:connectedCallback
            #js {:configurable true
                 :value
                 #(this-as this (notify this))}}))
     (.set default-values constructor (atom {}))
     constructor)))

(defn assign-method!
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

(defn observe-attributes!
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

(defn add-observed-attribute!
  [c attribute]
  (js/console.log (.-observedAttributes c))
  (js/Object.defineProperty
   c
   "observedAttributes"
   #js {:configurable true
        :value (.concat  #js [attribute] (or (.-observedAttributes c) #js []))})
  (js/console.log c)
  c)

(defn assign-property!
  "Assigns a reactive property to the element"
  ([c property value] (assign-property! c property value identity))
  ([c property value setter]
   (let [proto (.-prototype c)]
     (swap! (.get default-values c) assoc (keyword property) value)
     (add-observed-attribute! c property)
     (js/Object.defineProperty
      proto
      (name property)
      #js {:configurable true
           :get #(this-as
                  this
                  (when (not (contains? @(.get cache this) (keyword property)))
                    (swap! (.get cache this) assoc (keyword property) ((keyword property) @(.get default-values c))))
                  ((keyword property) @(.get cache this)))
           :set #(this-as
                  this
                  (swap! (.get cache this) assoc (keyword property) (setter % this))
                  (notify this))}))
   c))

(defn connect!
  "Assigns connectedCallback to element"
  [c cb]
  (assign-method! c :connectedCallback cb))

(defn disconnect!
  "Assigns disconnectedCallback to element"
  [c cb]
  (assign-method! c :disconnectedCallback cb))

(defn define!
  "Registers as custom element"
  [c element-name]
  (when (not (js/window.customElements.get element-name))
    (js/window.customElements.define element-name c)))