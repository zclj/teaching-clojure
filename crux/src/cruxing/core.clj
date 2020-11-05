(ns cruxing.core
  (:require [crux.api :as crux]))

;; Crux Clojure API Ref: https://www.opencrux.com/reference/clojure-api.html

;; start

;; https://www.opencrux.com/reference/configuration.html
;; "Without any explicit configuration, Crux will start an in-memory node."
(def crux-node (crux/start-node {}))

;; check the (surprise!) status of the node
(crux/status crux-node)

(comment
  ;; is closable (java.io.Closeable)
  (.close crux-node))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Transactions

;; https://www.opencrux.com/reference/transactions.html

;;;;
;; Some data

(def my-object
  {:aspect/name "Object 1"
   :aspect/id   (java.util.UUID/randomUUID)})

;;;;
;; Put -> Write a document

;; all data going into crux need a :crux.db/id. It can be a keyword, string, int, uuid, etc.

;; since we are in a data-driven language and ecosystem, guess what, transactions are data

(def my-transaction
  [:crux.tx/put ; operation
   (assoc my-object :crux.db/id (:aspect/id my-object))
   ; we can put a 'valid-time' here, defaults to transaction time
   ])

(comment
  my-transaction

  ;; lets submit the tx. Note that there is a vector of tx-data
  (crux/submit-tx crux-node [my-transaction])
  )

;;;;
;; Query

;; crux store documents and is also a graph database

;; Queries are performed using Datalog
;; For more on Datalog check out http://www.learndatalogtoday.org/

;; Datalog is also used by Datomic and DataScript

;; https://www.opencrux.com/reference/queries.html
;; "The documents themselves are represented in the database indexes as "entity–attribute–value" (EAV) facts. For example, a single document {:crux.db/id :myid :color "blue" :age 12} is transformed into two facts [[:myid :color "blue"][:myid :age 12]]."

;; Note that queries run on a 'point-in-time db'
(crux/q
 (crux/db crux-node)
 '{:find  [o]
   :where [[o :aspect/id]]})

(crux/q
 (crux/db crux-node)
 '{:find  [o]
   :where [[o :aspect/name "Object 1"]]})

(crux/q
 (crux/db crux-node)
 '{:find  [n]
   :where [[o :aspect/name n]]})

;;;;
;; Lets add some more objects

(defn new-object
  [object-name]
  {:aspect/name object-name
   :aspect/id   (java.util.UUID/randomUUID)})

(defn make-object-tx
  [object]
  [:crux.tx/put
   (assoc object :crux.db/id (:aspect/id object))])

;; build the tx data
(def tx-data
  (mapv
   make-object-tx
   (map new-object ["Object 2" "Object 3" "Object 4"])))

(comment
  
  (crux/submit-tx crux-node tx-data)

  (crux/q
   (crux/db crux-node)
   '{:find  [o]
     :where [[o :aspect/id]]})

  (crux/q
   (crux/db crux-node)
   '{:find  [n]
     :where [[o :aspect/name n]]})
  )

;; Lets make some graphs

(def object-with-refs
  (assoc
   (new-object "Object With Refs")
   :aspect/refs
   [(:aspect/id my-object)]))

(comment
  (crux/submit-tx crux-node [(make-object-tx object-with-refs)])

  (crux/q
   (crux/db crux-node)
   '{:find  [n]
     :where [[o :aspect/name n]]})

  (crux/q
   (crux/db crux-node)
   '{:find  [n]
     :where [[_ :aspect/refs r]
             [o :aspect/id r]
             [o :aspect/name n]]})

  (crux/q
   (crux/db crux-node)
   '{:find  [n]
     :where [[o :aspect/name "Object 1"]
             [o :aspect/id id]
             [p :aspect/refs id]
             [p :aspect/name n]]})
  )

;; With a document id we can get the complete entity
(def qr
  (crux/q
   (crux/db crux-node)
   '{:find  [p]
     :where [[o :aspect/name "Object 1"]
             [o :aspect/id id]
             [p :aspect/refs id]]}))

(crux/entity (crux/db crux-node) (ffirst qr))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Time

;; we get Object 1 as of now
(crux/q
 (crux/db crux-node)
 '{:find  [o]
   :where [[o :aspect/name "Object 1"]]})

;; but Object 1 was not present 19 years ago
(crux/q
 (crux/db crux-node #inst "2001-10-10")
 '{:find  [o]
   :where [[o :aspect/name "Object 1"]]})

;; What has happend to Object 1?

(crux/entity-history (crux/db crux-node) (:aspect/id my-object) :desc)

;; lets update
(crux/submit-tx
 crux-node
 [(make-object-tx (assoc my-object :aspect/icon "Nice Icon!"))])

(crux/entity-history (crux/db crux-node) (:aspect/id my-object) :desc)

(crux/entity-history
 (crux/db crux-node) (:aspect/id my-object) :desc {:with-docs? true})

;; it is possible to set start and end times if we like

;; Going back to transactions, lets delete a document
(defn delete-object-tx
  [id]
  [:crux.tx/delete id])

;; so I want to delete an object but also add a new one in the same tx
;; working with data..

(def the-tx
  (into
   [(make-object-tx (new-object "I'm an object"))]
   [(delete-object-tx (:aspect/id my-object))]))

(crux/submit-tx crux-node the-tx)

;; gone..
(crux/q
 (crux/db crux-node)
 '{:find  [o]
   :where [[o :aspect/name "Object 1"]]})

;; the history show us when
(crux/entity-history (crux/db crux-node) (:aspect/id my-object) :desc)

;; I forgot some important data thingy something..
;; lets transact back in time, with valid time

(crux/submit-tx
 crux-node
 [[:crux.tx/put
   (assoc
    (assoc my-object :aspect/name "Forgot the name change")
    :crux.db/id (:aspect/id my-object))
   #inst "2020-11-05T10:47:45.620-00:00"]])

(crux/entity-history
 (crux/db crux-node) (:aspect/id my-object) :desc {:with-docs? true})

;; Homework!
;; Take the REST API example from the Rich and Morty folder
;; put it into Crux and do some cool graphs
