(ns examples.mm)

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; 'multimethods' is an open system
;; for custom polymorphism

(defmulti greet
  (fn [opts _]
    (:lang opts)))

(defmethod greet :en-us
  [_ name]
  (str "Howdy " name "!"))

(defmethod greet :se-sv
  [_ name]
  (str "Hej " name "!"))

(greet {:lang :se-sv} "Lennart")
(greet {:lang :en-us} "Lennart")

(defmethod greet :default
  [_ name]
  (str "Hello " name "!"))

(greet {} "Lennart")
(greet {:lang :se-fi} "Lennart")
