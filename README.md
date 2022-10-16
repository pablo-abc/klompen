# Klompen - Custom Elements with ClojureScript

Utilities to create custom elements with reactive properties and declarative templating using ClojureScript.

Pretty much a work in progress as of now. Not useable.

## Example usage

```clojure
(ns demo.core
  (:require
   [klompen.core :refer [create-component connect! define! assign-property!]]
   [klompen.styles :refer [set-styles!]]
   [klompen.html :refer [render!]]))

;; Define template using hiccup style syntax.
;; attributes and properties can react to state changes
;; by assigning a function that receives the element
;;
;; Events receive the element as first argument
;; and the event object as a second argument
(def template
  [:button {:on/click #(set! (.-pressed %) (not (.-pressed %)))
            :pressed #(.-pressed %)
            :prop/other (fn [host]
                          (.-pressed host))} "Click me"])

(defn init! []
  (->
  ;; create-component creates a custom element constructor
   (create-component
   ;; Its first argument can be a function that runs
   ;; on instantiation
    #(-> %
         (.attachShadow #js {:mode "open"})
         (render! template)))
  ;; helper methods  can be run on the constructor to
  ;; modify the element

  ;; connect! adds a connectedCallback method
   (connect! #(print "Connected"))
   ;; adds a disconnectedCallback method
   (disconnect! #(print "Disconnected"))
    ;; adds a property that triggers updates on
    ;; changes
   (assign-property! "pressed" false)
   (assign-property! "disabled" false)
   ;; sets styles to the element. Can be a list as well.
   (set-styles! "button{background:pink; border:none}")
   (define! "cljs-button"))

  (render!
   js/document.body
   [:div
    [:h1 "testing custom elements"]
    [:cljs-button]]))
```
