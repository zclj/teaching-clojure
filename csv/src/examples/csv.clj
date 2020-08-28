(ns examples.csv
  (:require [clojure.data.csv :as csv]
            [clojure.java.io :as io]
            [clojure.string :as s])
  (:gen-class))

;; Read som CSV
(csv/read-csv "this,is\na,test")

;; Other separator
(csv/read-csv "this;is\na;test" :separator \;)

(def space-missions
  (with-open [reader (io/reader "space.csv")]
    (doall
     (csv/read-csv reader))))

;; check the header
(first space-missions)

;; example of data row
(second space-missions)

;; after first and second we do nth
(nth space-missions 45)

;; working with maps is more fun so lets convert to maps

(zipmap [:a :b :c] [1 2 3])

;; so we want to take the header of the csv file and use it as keys
(first space-missions)

(comment
  (def header-example
    [""
     "Unnamed: 0"
     "Company Name"
     "Location"
     "Datum"
     "Detail"
     "Status Rocket"
     " Rocket"
     "Status Mission"])

  (def row-example
    ["0"
     "0"
     "SpaceX"
     "LC-39A, Kennedy Space Center, Florida, USA"
     "Fri Aug 07, 2020 05:12 UTC"
     "Falcon 9 Block 5 | Starlink V1 L9 & BlackSky"
     "StatusActive"
     "50.0 "
     "Success"])
  )

;; lets make keywords of the header to prepare for zipmap
(map keyword header-example)
;; => (: :Unnamed: 0 :Company Name :Location :Datum :Detail :Status Rocket : Rocket :Status Mission)

;; ugh.. this does not look great, lets improve

;; the first header is empty and the second have a bad name
(into ["index"] (drop 2 header-example))

;; we want ideomatic key names, snake-kebab-case
(map keyword
     (map #(clojure.string/replace % #" " "-")
          (map clojure.string/lower-case
               (map clojure.string/trim
                    (into ["index"] (drop 2 header-example))))))

;; written with thread last ->>
(def clean-header
  (->> (into ["index"] (drop 2 header-example))
       (map s/trim)
       (map s/lower-case)
       (map #(s/replace % #" " "-"))
       (map keyword)))

(reduce
 (fn [acc dirty-header]
   (let [trimmed  (s/trim dirty-header)
         lowered  (s/lower-case trimmed)
         dashed   (s/replace lowered #" " "-")
         header-k (keyword dashed)]
     (conj acc header-k)))
 []
 (into ["index"] (drop 2 header-example)))

(defn header->keyword
  [header-string]
  (let [trimmed  (s/trim header-string)
        lowered  (s/lower-case trimmed)
        dashed   (s/replace lowered #" " "-")
        header-k (keyword dashed)]
     header-k))

(map header->keyword (into ["index"] (drop 2 header-example)))

(zipmap clean-header (rest (second space-missions)))

(def clean-missions
  (map #(zipmap clean-header (rest %)) (rest space-missions)))



(defn -main
  "I don't do a whole lot ... yet."
  [& args]
  (println "Hello, World!"))
