(ns ram.core
  (:require [clj-http.client :as http]
            [cheshire.core :as json]))

;; see: https://github.com/dakrone/clj-http for more
;; info on clj-http 
;; 
;; typical usages:
;; - (http/get url request)
;; - (http/post url request)
;; ...

;; rick and morty api: https://rickandmortyapi.com/documentation/#rest
(def base-url "https://rickandmortyapi.com/api")

;; sample code:
;; get the first character (rick)
(def rick-response
  (http/get (str base-url "/character/1")))

(let [body (:body rick-response) ;; extract body (which is a json string)
      rick (json/parse-string body true)] ;; transform body (json) to edn
  rick)

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; ASSIGNMENTS!!!
;; 

;; asignment 1: 
;; create a function that returns character
;; information as edn data given a character id

;; asignment 2:
;; create a function that returns location
;; information given a character edn map

;; assignment 3:
;; create a function that returns a list of
;; episode names given a character edn map

;; assignment 4:
;; create a function that returns a list of
;; residents on the location of the given 
;; character

;; assignment 5:
;; create a function that returns a map that
;; maps a character name to the characters
;; species and status, given a list of character
;; edn maps
;; 
;; i.e. the result map should be something like:
;; {"Rick Sanches" {:species "Human"
;;                  :status "Alive"}
;;  "Adjudicator Rick" {:species "Human"
;;                      :status "Dead"}
;;  ...}
