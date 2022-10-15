(ns demo.core
  (:require
   [klompen.core :refer [create-component connect! define! assign-property! observe-attributes!]]
   [klompen.html :refer [render]]))

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
         (assign-property! "pressed" false)
         (render template)))
   (connect! #(print "Connected"))
   (observe-attributes! ["asdasd"] #(js/console.log ""))
   (define! "cljs-button"))
  (render
   js/document.body
   [:div {:class "test-class"}
    [:h1 "testing custom elements"]
    [:cljs-button]]))