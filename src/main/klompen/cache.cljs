(ns klompen.cache)

(def cache (js/Reflect.construct js/WeakMap #js []))

(def bindings (js/Reflect.construct js/WeakMap #js []))