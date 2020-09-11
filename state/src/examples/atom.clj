(ns examples.atom)

(defn make-user
  [e-mail name]
  {:user/e-mail e-mail
   :user/name   name})

(comment
  (make-user "snabel@kabel.se" "Lola")
  )

(def users [(make-user "snabel@kabel.se" "Lola")
            (make-user "snyting@kabel.se" "Rocket Rolf")
            (make-user "snigel@kabel.se" "Racing Rolf")])

;; immutable list of users do not change
(conj users (make-user "kongo@kabel.se" "Bongo"))

;; atom manages state in a thread-safe manner, by leveraging CAS semantics (more later)
(def users-db (atom []))

;; reset! sets the state of the atom to the given value
(reset! users-db users)

;; swap! sets the state of the atom to the value of applying the given function to the current value
(swap! users-db conj (make-user "bad@ass.nu" "Rolf Minigolf"))

;; the get the value of an atom we can derefference it
(deref users-db)

;; or with reader literal
@users-db


(defn add-user
  [db user]
  (swap! db conj user))

(defn remove-user
  [db e-mail]
  (swap!
   db
   (fn [current]
     (filterv (fn [user] (not= (:user/e-mail user) e-mail)) current))))

(comment
  (add-user users-db (make-user "a@b.c" "the name"))
  
  (remove-user users-db "a@b.c")

  )

;; swap! internals CAS
;; 1. derefs current value -> [current]
;; 2. apply given fn on current value -> [next]
;; 3. compare [current] with @atom
;; 4a. If [current] = @atom, make the swap to [next]
;; 4b. If not, re-run given fn with @atom as new [current] (restart from 1)

;; Moral of the story -> your fn SHOULD be side-effect free since it can be retried n number of times.
;; In addition, avoid a large number of threads "banging" on the same atom, there will be a large number of retries.

;;;;
;; Encore

;; Reflect on how much nicer this would be if we had an associative data structure (as the hash-maps from last session)
(defn alter-user
  [db e-mail username]
  (swap!
   db
   (fn [current]
     (let [user (first (filterv (fn [user] (= (:user/e-mail user) e-mail)) current))]
       (if user
         (let [new-user (assoc user :user/name username)
               new-db   (filterv (fn [user] (not= (:user/e-mail user) e-mail)) current)]
           (conj new-db new-user))
         current)))))

(comment
  (alter-user users-db "bad@ass.nu" "Roffe")
  )
