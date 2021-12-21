(ns examples.basics)

;; Clojure is a LISP, LISt Processor
;; LISP first appeared in 1958

;; Code is expressed in lists (surprise!) called forms.
;; The first element in a form is a function, the rest is the arguments.
(+ 2 2)
;; => 4

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

1 ;; => 1

1.0 ;; => 1.0

"Hi Mum!" ;; => "Hi Mum!"

2/3 ;; => 2/3

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

;; lists for code
'(1 2 3)
;; => (1 2 3)

;; Vector for "lists"
[1 2 3]
;; => [1 2 3]

;; Set for set semantic
#{1 2 3}
;; => #{1 3 2}

;; Hash maps for composit data
{:name "Bobo"
 :age  37};; => {:name "Bobo", :age 37}
