(ns demo.core
  (:require
   [klompen.core :refer [create-component connect! define! assign-property! disconnect!]]
   [klompen.styles :refer [set-styles!]]
   [klompen.html :refer [render!]]))

(def template
  [:button {:on/click #(set! (.-pressed %) (not (.-pressed %)))
            :pressed #(.-pressed %)
            :prop/other #(.-pressed %)} "Click me"])

#_{:clj-kondo/ignore [:clojure-lsp/unused-public-var]}
(defn init! []
  (->
   (create-component
    #(-> %
         (.attachShadow #js {:mode "open"})
         (render! template)))
   (connect! #(print "Connected"))
   (disconnect! #(print "Disconnected"))
   (assign-property! "pressed" false)
   (assign-property! "disabled" false)
   (set-styles! "button{background:pink; border:none}")
   (define! "cljs-button"))

  (render!
   js/document.body
   [:div
    [:h1 "testing custom elements"]
    [:cljs-button]]))