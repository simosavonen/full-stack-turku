(ns server.main
  (:require [http :as h]))

(defn handle-result [res]
  (if (= (.-statusCode res) 200)
    (println "Result retrieved successfully!" )))

(defn fetch-foli-data! []
  (-> (.get http "http://data.foli.fi/siri/vm"
            handle-result)))

(defn reload! []
  (println "Code updated.")
  (fetch-foli-data!))

(defn main! []
  (println "App loaded!"))
