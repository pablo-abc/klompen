(ns klompen.signal)

(defonce context (atom []))

(defn- subscribe [running subscriptions]
  (swap! subscriptions conj running)
  (swap! (:dependencies running) conj subscriptions))

(defn create-signal [v]
  (let [value (atom v)
        subscriptions (atom #{})]
    [(fn []
       (let [running (last @context)]
         (when (some? running) (subscribe running subscriptions))
         @value))
     (fn [nv]
       (reset! value (if (fn? nv) (nv @value) nv))
       (mapv (fn [sub]
               (let [exec (:execute sub)]
                 (when (fn? exec) (exec))))
             @subscriptions)
       '())]))

(defn- cleanup [running]
  (mapv
   #(swap! % disj running) @(:dependencies running))
  (reset! (:dependencies running) #{}))

(defn create-effect [effect-fn]
  (let [execute (fn [running]
                  (cleanup running)
                  (swap! context conj running)
                  (try
                    (effect-fn)
                    (finally (swap! context pop))))
        running {:dependencies (atom #{})}]
    (execute (assoc running :execute (fn [] (execute running))))))

(comment
  (let [[n set-n]
        (create-signal 0)]
    (create-effect #(print (n)))
    (set-n 2)
    (set-n 5)
    nil))