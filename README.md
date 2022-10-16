# Klompen - Custom Elements with ClojureScript

[![Clojars Project](https://img.shields.io/clojars/v/net.clojars.pabloabc/klompen.svg)](https://clojars.org/net.clojars.pabloabc/klompen)

Utilities to create custom elements with reactive properties and declarative templating using ClojureScript.

Pretty much a work in progress as of now. Super basic functionality.

## Usage

The API of klompen relies on chaining function to add functionality to a custom element. It all starts with a call to `create-ce`. This creates a function that works as a constructor that can be defined as a custom element.

`create-ce` optionally accepts a function as its first argument. This function is ran during instantiation, so it can be used to attach a shadow root and render to it. It receives the instance of the custom element as its argument.

```clojure
(create-ce
  (fn [el]
    (.attachShadow el #js {:mode "open"})))
```

Klompen also provides some extra utilities that help to modify the custom element. For example, `connect!` allows you to add a `connectedCallback` to the element.

```clojure
(def my-element (create-ce))

(connect!
  my-element
  (fn [el] (print "Connected:" el)))
```

These functions can be chained using the threading macro.

## Example usage

```clojure
(ns demo.core
  (:require
   [klompen.core :refer [create-ce connect! define! assign-property! disconnect!]]
   [klompen.styles :refer [set-styles!]]
   [klompen.html :refer [render!]]
   [garden.core :refer [css]]))

;; CSS is assigned as a string. Here we are using Garden
;; to simplify the creation of it.
(def styles (css [:* {:font-size "200%"}]
                 [:span {:width "4rem"
                         :display "inline-block"
                         :text-align "center"}]
                 [:button {:width "4rem"
                           :height "4rem"
                           :border "none"
                           :border-radius "10px"
                           :background-color "seagreen"
                           :color "white"}]))

;; Define template using hiccup style syntax.
;; attributes and properties can react to state changes
;; by assigning a function that receives the element
;;
;; Events receive the element as first argument
;; and the event object as a second argument
;;
;; You can assign a function as a child to an element
;; to make it update on property changes.
(def template [[:button {:on/click
                         #(set! (.-count %)
                                (dec (.-count %)))} "-"]
               [:span #(str (.-count %))]
               [:button {:on/click
                         #(set! (.-count %)
                                (inc (.-count %)))} "+"]])

(defn init! []
  (->
  ;; create-ce creates a custom element constructor
   (create-ce
    #(-> %
         (.attachShadow #js {:mode "open"})
         ;; The `render!` function renders the template
         ;; in the shadow root
         (render! template)))
   (assign-property! "count" 0)
   ;; sets styles to the element. Can be a list as well.
   (set-styles! styles)
   ;; connect! adds a connectedCallback method
   (connect! #(print "Connected"))
   ;; adds a disconnectedCallback method
   (disconnect! #(print "Disconnected"))
   (define! "my-counter"))

  (render!
   js/document.body
   [:my-counter]))
```
