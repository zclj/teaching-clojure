(ns organizer
  (:require [clojure.pprint :refer [pprint]]))

(defonce app-state (atom {:state :init}))

(def fsm {:init           {:start      :main}
          :main           {:todos      :todos}
          :todos          {:has-items  :todos/existing
                           :no-items   :todos/empty}
          :todos/empty    {:edit-new   :todos/empty
                           :commit-new :todos/existing
                           :main       :main}
          :todos/existing {:edit-new   :todos/existing
                           :commit-new :todos/existing
                           :ack-item   :todos/existing
                           :rem-item   :todos
                           :main       :main}})

(defn dispatch-trans
  [from trans to & _]
  [from trans to])

(defmulti transition-fn #'dispatch-trans)

(defmethod transition-fn :default
  [_ _ to state]
  (assoc state :state to))

;; (defmethod transition-fn [:init :start :main]
;;   [_ _ _ state]
;;   (assoc state :state :main))

;; (defmethod transition-fn [:main :todos :todos]
;;   [_ _ _ state]
;;   (assoc state :state :todos))

;; (defmethod transition-fn [:todos :has-items :todos/existing]
;;   [_ _ _ state]
;;   (assoc state :state :todos/existing))

;; (defmethod transition-fn [:todos :no-items :todos/empty]
;;   [_ _ _ state]
;;   (assoc state :state :todos/empty))

(defmethod transition-fn [:todos/empty :edit-new :todos/empty]
  [_ _ _ state item]
  (-> state
      (assoc :state :todos/empty)
      (assoc :todos/new item)))



(defn do-transition
  [from trans & args]
  (let [to (get-in fsm [from trans])]
    (swap! app-state (fn [s]
                       (if (= (:state s) from)
                         (apply transition-fn from trans to s args)
                         s)))))


(comment
  ;;

  @app-state
  (deref app-state)
  (reset! app-state {:state :main})

  (do-transition :init :start)

  (do-transition :main :foo)
  (do-transition :main :todos)
  (do-transition :todos :no-items)

  (do-transition :todos/empty :edit-new "bar")

  ;;  
  )