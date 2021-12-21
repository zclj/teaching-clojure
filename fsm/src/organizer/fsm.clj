(ns fsm)

(defn do-transition
  [fsm-def state-db from trans & args]
  (let [to (get-in fsm-def [from trans])]
    (assert to (str "no transition " trans " from state " from " is defined"))
    (swap! state-db (fn [s]
                      (assert (= (:state s) from) (str "current state is " (:state s) " not " from))
                      (let [fn   (:fn to (constantly s))
                            next (:next to)]
                        (assoc (apply fn s args) :state next))))))

(defn subscribe
  [reference state handler]
  (add-watch reference state (fn [_ _ old new]
                               (let [new-state (:state new)
                                     old-state (:state old)]
                                 (when (and (= new-state state) (not= new-state old-state))
                                   (handler new))))))

(defn unsubscribe
  [reference key]
  (remove-watch reference key))

