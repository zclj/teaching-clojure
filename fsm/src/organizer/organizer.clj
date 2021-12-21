(ns organizer
  (:require [clojure.pprint :refer [pprint]]
            [fsm]))

(defn edit-new
  [state item]
  (assoc state :todos/new item))

(defn commit-new
  [state]
  (-> state
      (assoc-in [:todos (:todos/new state)] {})
      (dissoc :todos/new)))

(defn cancel-new
  [state]
  (dissoc state :todos/new))

(defn rem-item
  [state item]
  (update state :todos dissoc item))

(defonce app-state (atom {:state :init}))

(def fsm {:init            {:start      {:next :todos/check}}
          :todos/check     {:has-items  {:next :todos/some}
                            :no-items   {:next :todos/empty}}
          :todos/empty     {:edit-new   {:next :todos/empty-new
                                         :fn   edit-new}}
          :todos/empty-new {:commit-new {:next :todos/some
                                         :fn   commit-new}
                            :cancel-new {:next :todos/empty
                                         :fn   cancel-new}}
          :todos/some      {:edit-new   {:next :todos/some-new
                                         :fn   edit-new}
                            :ack-item   {:next :todos/some}
                            :rem-item   {:next :todos/check
                                         :fn   rem-item}}
          :todos/some-new  {:commit-new {:next :todos/some
                                         :fn   commit-new}
                            :cancel-new {:next :todos/some
                                         :fn   cancel-new}}})

(def do-transition (partial fsm/do-transition fsm app-state))

(defn handle-todos-check!
  [state]
  (if (seq (:todos state))
    (do-transition :todos/check :has-items)
    (do-transition :todos/check :no-items)))

(defn start
  []
  (reset! app-state {:state :init})
  (fsm/subscribe app-state :todos/check handle-todos-check!))

(defn stop
  []
  (fsm/unsubscribe app-state :todos/check))

(defn restart
  []
  (stop)
  (start))

(comment
  ;;

  (add-watch app-state :foo (fn [_ _ _ n] (pprint n)))
  (remove-watch app-state :foo)

  (restart)

  @app-state
  (deref app-state)

  (reset! app-state {:state :init})

  (do-transition :init :foo)

  (do-transition :init :start)
  ;; (do-transition :todos/check :no-items)
  (do-transition :todos/empty :edit-new "bar")
  (do-transition :todos/empty-new :commit-new)
  (do-transition :todos/some :edit-new "baz")
  (do-transition :todos/some-new :cancel-new)
  (do-transition :todos/some-new :commit-new)

  (do-transition :todos/some :rem-item "baz")
  (do-transition :todos/some :rem-item "bar")


  ;;  
  )