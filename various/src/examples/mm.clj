(ns examples.mm)

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; 'multimethods' is an open system
;; for custom polymorphism
;; (one of Clojures' solution to https://en.wikipedia.org/wiki/Expression_problem)

(defmulti greet
  (fn [_ & {:keys [lang]}]
    lang))

(defmethod greet :en-us
  [name & _]
  (str "Howdy " name "!"))

(defmethod greet :se-sv
  [name & _]
  (str "Hej " name "!"))

(greet "Lennart" :lang :se-sv)
(greet "Lennart" :lang :en-us)

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; default function
;; 

(greet "Lennart")
(greet "Lennart" :lang :se-fi)

(defmethod greet :default
  [name & _]
  (str "Hello " name "!"))

(greet "Lennart")
(greet "Lennart" :lang :se-fi)
