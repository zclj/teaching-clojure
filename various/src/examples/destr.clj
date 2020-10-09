(ns examples.destr)

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; what problem is destructuring trying to solve?
;; 

(def entries [[:foo 1] [:bar 2]])

;; picking out the pieces by hand
(let [e1  (first entries)
      e1k (first e1)
      e1v (second e1)
      e2  (second entries)
      e2k (first e2)
      e2v (second e2)]
  [e1k '+ e2k '= (+ e1v e2v)])

;; picking out the pieces with some destructuring
(let [[e1k e1v] (first entries)
      [e2k e2v] (second entries)]
  [e1k '* e2k '= (* e1v e2v)])

;; use destructuring all the way
(let [[[e1k e1v] [e2k e2v]] entries]
  [e1k '- e2k '= (- e1v e2v)])

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; sequential length mismatch
;; 

(def small-list '(1 2 3))

(def large-list '(1 2 3 4 5 6 7 8 9))

;; if seequence is to small, values are nil
(let [[a b c d e] small-list]
  (vector a b c d e))

;; ignore "left-over" items
(let [[a b c d e] large-list]
  (vector a b c d e))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; ignore and rest
;; 

(def items [:foo 1 :bar 2 :baz 3 :gizmo 4])

;; destruct into first & rest
(let [[first & rest] items]
  (apply prn [first rest]))

;; ignore items
(let [[k1 _ k2 _ k3 _ k4] items]
  (apply prn [k1 k2 k3 k4]))

;; combining ignore and rest
(let [[_ _ k2 & rest] items]
  (apply prn [k2 rest]))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; associative destructuring
;; 

(def item {:foo 1
           :bar 2
           :baz {:gizmo 3}})

;; pick out the values
(let [foo (:foo item)
      bar (:bar item)
      baz (:baz item)]
  (apply prn [foo bar baz]))

;; use associative destructuring instead
(let [{foo :foo
       bar :bar
       baz :baz} item]
  (apply prn [foo bar baz]))

;; non-existing keys are nils
(let [{yada :yada} item]
  (prn yada))

;; but can also defined a fallback value
(let [{yada :yada :or {yada "Not Found"}} item]
  (prn yada))

;; fallback values are only for non-existing keys
(let [{foo :foo
       yada :yada
       :or {foo 14
            yada "Not Found"}} item]
  (apply prn [foo yada]))

;; even better sequential destructuring when using keywords
(let [{:keys [foo bar baz]} item]
  (apply prn [foo bar baz]))

;; fallbacks are the same
(let [{:keys [foo bar baz yada]
       :or {foo 14 yada "Not Found"}} item]
  (apply prn [foo bar baz yada]))

;; nesting associative destructuring
(let [{{:keys [gizmo]} :baz} item]
  (prn gizmo))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; keyword args
;; 

(defn my-fn1
  [x y opts]
  (let [{:keys [debug verbose]} opts]
    (when debug (prn "debug logging"))
    (when verbose (prn "verbose logging"))
    (+ x y)))

(my-fn1 1 2 {})
(my-fn1 1 2 {:debug true})
(my-fn1 1 2 {:debug true :verbose true})
(my-fn1 1 2 {:debug false :verbose true})

(defn my-fn2
  [x y {:keys [debug verbose]}]
  (when debug (prn "debug logging"))
  (when verbose (prn "verbose logging"))
  (+ x y))

(my-fn2 1 2 {})
(my-fn2 1 2 {:debug true})
(my-fn2 1 2 {:debug true :verbose true})
(my-fn2 1 2 {:debug false :verbose true})

(defn my-fn3
  [x y & opts]
  (let [{:keys [debug verbose]} opts]
    (when debug (prn "debug logging"))
    (when verbose (prn "verbose logging"))
    (+ x y)))

(my-fn3 1 2)
(my-fn3 1 2 :debug true)
(my-fn3 1 2 :debug true :verbose true)
(my-fn3 1 2 :verbose true)

(defn my-fn4
  [x y & {:keys [debug verbose]}]
  (when debug (prn "debug logging"))
  (when verbose (prn "verbose logging"))
  (+ x y))

(my-fn4 1 2)
(my-fn4 1 2 :debug true)
(my-fn4 1 2 :debug true :verbose true)
(my-fn4 1 2 :verbose true)

