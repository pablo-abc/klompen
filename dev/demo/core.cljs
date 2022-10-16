(ns demo.core
  (:require
   [klompen.core :refer [create-ce connect! define! add-property! disconnect!]]
   [klompen.styles :refer [set-styles!]]
   [klompen.html :refer [render! set-html!]]
   [garden.core :refer [css]]))

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

(def template [[:button {:on/click
                         #(set! (.-count %)
                                (dec (.-count %)))} "-"]
               [:span #(str (.-count %))]
               [:button {:on/click
                         #(set! (.-count %)
                                (inc (.-count %)))} "+"]])

#_{:clj-kondo/ignore [:clojure-lsp/unused-public-var]}
(defn init! []
  (->
   (create-ce)
   (add-property! "count" 0)
   (set-styles! styles)
   (set-html! template)
   (connect! #(print "Connected"))
   (disconnect! #(print "Disconnected"))
   (define! "my-counter"))

  (render!
   js/document.body
   [:div
    [:h1 "testing custom elements"]
    [:my-counter]]))