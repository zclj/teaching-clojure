(ns exceptions.core
  (:require [clj-http.client :as client]))

(def foo {:foo 1 :bar 3})

(def bar (with-meta foo {:src :copy}))

(defn print-stuff
  [s]
  (println (meta s)))

(defn result
  [x status]
  (with-meta x {:status status}))

(defn result?
  [x status]
  (= (:status (meta x)) status))

(def success #(result % :success))
(defn success?
  [x]
  (let [status (:status (meta x) :success)]
    (= status :success)))

(def fail #(result % :fail))
(def fail? #(result? % :fail))

(defn get-stuff
  [path]
  (case path
    nil?  (fail {:message "No path"})
    "foo" (fail {:message "Invalid path"})
    :else (success {:path path})))

(def no-exceptions {:throw-exceptions false})

(defn http-send
  [request]
  (try
    (success (client/request (merge request no-exceptions)))
    (catch IllegalArgumentException ex
      (fail {:exception ex}))
    (catch java.net.UnknownHostException ex
      (fail {:exception ex}))
    (catch java.net.ConnectException ex
      (fail {:exception ex}))))

(defmacro while->
  [test expr & forms]
  (let [g (gensym)
        steps (map (fn [step] `(if (~test ~g) (-> ~g ~step) ~g))
                   forms)]
    `(let [~g ~expr
           ~@(interleave (repeat g) (butlast steps))]
       ~(if (empty? steps)
          g
          (last steps)))))

(comment
  ;;

  (-> {}
      (assoc :foo 1)
      (assoc :bar 2))

  (assoc
   (assoc [] :foo 1)
   :bar 2)

  (cond-> {:foo 1}
    :foo (assoc :bar 2)
    :bar (assoc :baz 3))

  (let [res (http-send {:method :get
                        :url "http://google.com"})]
    (if (success? res)
      (let [res2 (http-send {:method :get
                             :url "http://trams"})]
        (if (success? res2)
          {:res res :res2 res}
          (println (get res :exception))))
      (println (get res :exception))))

  (defn lookup-x
    []
    (println "lookup-x")
    {:x
     (http-send {:method :get
                 :url "http://google.com"})})

  (defn lookup-y
    [res]
    (println "lookup-y")
    (let [y (http-send {:method :get
                        :url "http://trams"})]
      (with-meta (assoc res :y y) (meta y))))

  (defn lookup-z
    [res]
    (println "lookup-z")
    (http-send {:method :get
                :url "http://duckduckgo.com"}))

  (some-> (lookup-x)
          (lookup-y)
          (lookup-z))

  (while-> success?
           (lookup-x)
           (lookup-y)
           (lookup-z))

  ;;
  )

