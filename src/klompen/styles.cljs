(ns klompen.styles)

(defn supports-adopting-style-sheets?
  "Tells wether the current browser supports constructable style sheets"
  []
  (and
   (some? js/window.ShadowRoot)
   (or
    (nil? js/window.ShadyCSS)
    (some? js/window.ShadyCSS.nativeShadow))
   (.hasOwnProperty js/Document.prototype "adoptedStyleSheets")
   (.hasOwnProperty js/CSSStyleSheet.prototype "replace")))

(defonce cache (atom {}))

(defn create-style-sheet
  ([css-text]
   (if (get @cache css-text)
     (get @cache css-text)
     (let [style-sheet
           (js/Reflect.construct js/CSSStyleSheet #js [])]
       (.replaceSync style-sheet css-text)
       (swap! cache assoc css-text style-sheet)
       style-sheet))))

(defn- append-styles! [root style-text]
  (let [style (js/document.createElement "style")]
    (set! (.-textContent style) style-text)
    (.appendChild root style)
    root))

(defn adopt-styles!
  [root styles]
  (let [styles-v (if (coll? styles) styles [styles])]
    (if (supports-adopting-style-sheets?)
      (set!
       (.-adoptedStyleSheets root)
       (clj->js
        (mapv
         #(if (instance? js/CSSStyleSheet %)
            %
            (create-style-sheet %)) styles-v)))
      (mapv #(append-styles! root %) styles-v)))
  root)

(defn set-styles!
  [c styles]
  (js/Object.defineProperty
   c
   "styles"
   #js {:configurable true
        :value styles}))