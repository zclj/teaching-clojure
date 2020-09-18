(ns examples.spec
  (:require [clojure.spec.alpha :as s]
            [clojure.spec.gen.alpha :as gen]))

;; Clojure spec guide: https://clojure.org/guides/spec

;; spec is based on predicates
;; a predicate is itself a spec
(s/valid? even? 10)
(s/valid? even? 9)

;; a richer "valid"-check
(s/conform even? 10)
(s/conform even? 9)

;; even more information
(s/explain even? 10)
(s/explain even? 9)

;; a set can be used as a predicate
(s/valid? #{:foo :bar :baz} :bar)
(s/valid? #{:foo :bar :baz} :gizmo)


;; specs can (and should) be registered
(s/def ::even even?)
(s/valid? ::even 10)
(s/valid? ::even 9)

(s/def ::my-set #{:foo :bar :baz})
(s/valid? ::my-set :foo)
(s/valid? ::my-set :gizmo)

;; can also generate sample values for a spec
(gen/generate (s/gen ::my-set))
(gen/sample (s/gen ::my-set))
(gen/sample (s/gen ::my-set) 20)


;; composing specs/predicates
(s/def ::positive #(>= % 0))
(s/def ::even-positive (s/and ::even ::positive))
(s/valid? ::even-positive 4)
(s/valid? ::even-positive -2)
(s/valid? ::even-positive 9)

(s/explain ::even-positive -2)

(s/def ::name-or-id (s/or :name string? :id int?))
(s/valid? ::name-or-id "foobar")
(s/valid? ::name-or-id 10)

(s/conform ::name-or-id "foobar")
(s/conform ::name-or-id 10)
(s/conform ::name-or-id {:foo :bar})

;; generating values still works
(gen/sample (s/gen ::name-or-id))
(map #(s/conform ::name-or-id %) (gen/sample (s/gen ::name-or-id)))
