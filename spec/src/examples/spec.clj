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
(s/explain-data even? 9)

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
(s/explain-data ::even-positive -2)

(s/def ::name-or-id (s/or :name string? :id int?))
(s/valid? ::name-or-id "foobar")
(s/valid? ::name-or-id 10)

(s/conform ::name-or-id "foobar")
(s/conform ::name-or-id 10)
(s/conform ::name-or-id {:foo :bar})

;; generating values still works
(gen/sample (s/gen ::name-or-id))
(map #(s/conform ::name-or-id %) (gen/sample (s/gen ::name-or-id)))


;; spec maps
(s/def ::name string?)
(s/def ::nick string?)
(s/def ::id int?)
(s/def ::person (s/keys :req [::name ::id]
                        :opt [::nick]))

(s/valid? ::person {::name "foo" ::id 12 :bar "tjosan"})
(s/valid? ::person {::name "foo" ::id 12 ::even-positive 14})
(s/conform ::person {::name "foo" ::id 12 ::even-positive 14})

(gen/generate (s/gen ::person))

;; spec collections
(s/def ::person-2 (s/cat :id ::id :name ::name :nick ::nick :person ::person))

(gen/generate (s/gen ::person-2))
(s/conform ::person-2 (gen/generate (s/gen ::person-2)))

;; modules/{module}/desired/model/{id}
(s/def ::topic (s/cat :modules #{"modules"} :module string? :desired #{"desired"} :model #{"model"} :object-id int?))

(gen/generate (s/gen ::topic))