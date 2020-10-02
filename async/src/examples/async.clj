(ns examples.async
  (:require [clojure.core.async
             :as async
             :refer [chan timeout close! thread <!! >!! alts!! alt!! go <! >! go-loop alts! alt!]]
            [clojure.pprint :as pp]))

(defn echo
  [val]
  (pp/pprint (str "ECHO: " (or val "nil")))
  val)

;; channels
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

;; by default a channel has one slot/entry, will block
;; on puts and takes
(def ch (chan))

;; but you can specify size, will only block once the
;; buffer is full
(def sized-ch (chan 2))

;;
;; can also specify channels with dropping semantics,
;; i.e. channels that never block on puts

;; when buffer is full any new messages are dropped,
;; keeping the oldest values
(def oldest-ch (chan (async/dropping-buffer 2)))
(>!! oldest-ch 1)
(>!! oldest-ch 2)
(>!! oldest-ch 3)
(<!! oldest-ch)

;; when buffer is full a new message causes the oldest message to be dropped,
;; keeping the newst values
(def newest-ch (chan (async/sliding-buffer 2)))
(>!! newest-ch 1)
(>!! newest-ch 2)
(>!! newest-ch 3)
(<!! newest-ch)

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; regular threads

;; <!! and >!! are regular thread-blocking take and put
(thread (echo (<!! ch)))
(thread (>!! ch :foo))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; ioc threads

;; <! and >! are "parking" take and put (i.e. similar to
;; async/await in C#)
(go (echo (<! ch)))
(go (>! ch :bar))

;; if we try to use <! and >! without go -> compiler error
(echo (<! ch))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; closing channels

;; when a channel is not to be used anymore we can close
;; it using close!, this will make the channel stop accepting
;; any more puts (puts will return false)
(let [c (chan)]
  (close! c)
  (>!! c :baz))

;; a closed channel returns nil on takes (i.e. does not block)
(let [c (chan)]
  (close! c)
  (<!! c))

;; a buffered channel still has its values after being closed
(let [c (chan 2)]
  (>!! c 1)
  (>!! c 2)
  (close! c)
  (echo (<!! c))
  (echo (<!! c))
  (echo (<!! c)))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; timeouts

;; a timeout is a channel that closes after specified
;; number of milliseconds
(let [t (timeout 2000)]
  (thread
    (<!! t)
    (pp/pprint "Timed out!")))

;; a timeout is still usable as a channel
(let [t (timeout 2000)]
  (thread
    (>!! t 1)
    (>!! t 2)
    (>!! t 3))
  (go-loop []
    (let [v (<! t)] ;; will loop as long as taken value is not nil
      (echo v)
      (if v
        (recur)
        (echo "Channel timed out, leaving loop")))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; alts/alt

;; alts! and alts!! let you wait on first action on
;; multiple channels
(let [c (chan)
      t (timeout 500)]
  (thread
    (let [[v ch] (alts!! [c t])]
      (if v
        (echo v)
        (echo "Timed out"))))
  (thread
    (Thread/sleep 400)
    (>!! c :foo)))

;; alt! allows you to provide an expression
;; to calculate a result for each operation

(def incoming (chan 10))
(def outgoing (chan 10))
(def lifetime (chan))

(go-loop []
  (alt!
    incoming ([message]
              (echo (str "Incoming message: " message))
              (recur))

    outgoing ([command]
              (echo (str "Outgoing command: " command))
              (recur))

    lifetime ([_]
              (echo "Shutting down..."))))

(>!! incoming :foo)
(>!! outgoing :bar)
(>!! incoming :baz)
(close! lifetime)

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; core.async is a library, it did not require any
;; changes to the language itself
;; 
;; in my view, core.async is sort of a mix of C# async/await
;; and Rx, it can be used for both purposes (and more)
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
