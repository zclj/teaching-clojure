(ns examples.maps)

;; maps as dictionary/collection
(def persons-1 {1 "Stefan Karlsson"
                2 "Ola Ottemalm"
                3 "Joakim Olsson"
                4 "Mårten Larsson"
                5 "Magne Hansen"})

;; maps as entities/structures
(def stefan-karlsson {:firstname "Stefan"
                      :lastname "Karlsson"
                      :e-mail "stefan.l.karlsson@foobar.com"})

(def persons-2 [{:firstname "Stefan"
                 :lastname "Karlsson"
                 :e-mail "stefan.l.karlsson@foobar.com"}
                {:firstname "Ola"
                 :lastname "Ottemalm"
                 :e-mail "amorokh@foobar.com"}])

;; nesting maps
(def persons-3 [{:firstname "Stefan"
                 :lastname "Karlsson"
                 :e-mail "steka@foobar.com"
                 :address {:street "Never visit me street 1"
                           :zip 12345
                           :city "Sala"}
                 :phone {:home "555-678"}}
                {:firstname "Ola"
                 :lastname "Ottemalm"
                 :e-mail "olot@foobar.com"
                 :address {:street "Hökmora 17"
                           :zip 23456
                           :city "Norberg"}
                 :phone {:home "555-678"
                         :iphone "5555-778"}}
                {:firstname "Joakim"
                 :lastname "Olsson"
                 :e-mail "jools@foobar.com"
                 :address {:street "Fluff Ave 5"
                           :zip 34567
                           :city "Västerås"}
                 :phone {:home "555-678"}}
                {:firstname "Mårten"
                 :lastname "Larsson"
                 :e-mail "mala@foobar.com"
                 :address {:street "Route 66"
                           :zip 45678
                           :city "Västerås"}
                 :phone {:home "555-678"}}])

;; get Joakims street
(get-in (first (filter #(= "jools@foobar.com" (:e-mail %)) persons-3)) 
        [:address :street])

;; normalize persons-3
(def persons-3n (reduce (fn [acc p] (assoc acc (:e-mail p) p)) {} persons-3))

;; get Joakims street
(get-in persons-3n ["jools@foobar.com" :address :street])

;; get Stefans home number
(get-in persons-3n ["steka@foobar.com" :phone :home])

;; Mårten gets a new phone (android) with a new number
(assoc-in persons-3n ["mala@foobar.com" :phone :android] "555-666")

;; Ola lost his iphone (!)
(assoc persons-3n 
       "olot@foobar.com"
       (assoc (persons-3n "olot@foobar.com")
              :phone
              (dissoc (get-in persons-3n ["olot@foobar.com" :phone])
                      :iphone)))

(assoc-in persons-3n
          ["olot@foobar.com" :phone]
          (dissoc (get-in persons-3n ["olot@foobar.com" :phone])
                  :iphone))

;; no dissoc-in ?
(update-in persons-3n ["olot@foobar.com" :phone] #(dissoc % :iphone))

;; we can also make use of variadic arguments and fns as first class constructs
(update-in persons-3n ["olot@foobar.com" :phone] dissoc :iphone)
