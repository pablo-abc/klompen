(ns klompen.signal)

(defonce ^:private context (atom []))

(defn- subscribe [running subscriptions]
  (swap! subscriptions conj running)
  (swap! (:dependencies running) conj subscriptions))

(defn create-signal
  "Creates a signal. Which is a function that contains
   a value whose changes can be tracker
   
   Example:
   ```
   (let [[value set-value] (create-signal 0)]
     (print (value)) ; => 0
     (set-value 5)
     (print (value)) ; => 5
   )
   ```"
  [v]
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

(defn create-effect
  "Accepts a function that runs every time its dependencies change.
   Its dependencies being signals being read within the function
   
   Example
   ```
   (let [[value set-value] (create-signal 0)]
     (create-effect #(print (value)))
     (set-value 1)
     (set-value 2))
     ;; Prints:
     ;; => 0
     ;; => 1
     ;; => 2
   ```"
  [effect-fn]
  (let [execute (fn [running]
                  (cleanup running)
                  (swap! context conj running)
                  (try
                    (effect-fn)
                    (finally (swap! context pop))))
        running {:dependencies (atom #{})}]
    (execute (assoc running :execute (fn [] (execute running))))))