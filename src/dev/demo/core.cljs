(ns demo.core
  (:refer-clojure :exclude [abs])
  (:require
   [klompen.core :refer [create-component connect! define! assign-property!]]
   [klompen.styles :refer [set-styles!]]
   [klompen.html :refer [render!]]))

(def template
  [:button {:on/click #(set! (.-pressed %) (not (.-pressed %)))
            :pressed #(.-pressed %)
            :prop/other (fn [host]
                          (.-pressed host))} "Click me"])

#_{:clj-kondo/ignore [:clojure-lsp/unused-public-var]}
(defn init! []
  (->
   (create-component
    #(-> %
         (.attachShadow #js {:mode "open"})
         (render! template)))
   (connect! #(print "Connected"))
   (assign-property! "pressed" false)
   (assign-property! "disabled" false)
   (set-styles! "button{background:pink; border:none}")
   (define! "cljs-button"))
  (render!
   js/document.body
   [:div {:class "test-class"}
    [:h1 "testing custom elements"]
    [:cljs-button]]))