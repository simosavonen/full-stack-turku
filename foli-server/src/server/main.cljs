(ns server.main
  (:require [http :as h]
            [zlib :as z]
            ["socket.io" :as sio]
            [express :as e]))

(defonce vehicles (atom []))
(defonce timer (atom nil))
(defonce services (atom {}))

(defn register! [name service]
  (swap! services assoc name service))
(defn service [name]
  (get @services name))

(defn stop-services! []
  (when-let [server (service :server)]
    (println "Close server...")
    (.close server))
  (when-let [timer (service :timer)]
    (println "Stop timer...")
    (js/clearTimeout timer)))

(defn handle-parsed-json
  "Takes the parsed JSON file and stores the vehicles in vehicles atom. Emits the data to listeners."
  [json-data]
  (reset! vehicles [])
  (doall (for [entry (get-in json-data [:result :vehicles])]
    (let [k (first entry)
          v (second entry)
          latitude (get v :latitude)
          longitude (get v :longitude)]
      (when (and latitude longitude)
        (swap! vehicles conj
               {:vehicle-id k
                :latitude latitude
                :longitude longitude})))))
  (println "Parsed" (count @vehicles) "vehicle locations")
  (when-let [ws (service :websocket)]
    (.emit ws "vehicles" (clj->js @vehicles))))

(defn handle-result
  "Takes the HTTP response and pipes it through GZIP since the content is compressed."
  [res]
  (when (= (.-statusCode res) 200)
    (let [raw-data (atom "")
          unzip-stream (z/createGunzip)]
      (.pipe res unzip-stream)
      (.on unzip-stream "data" (fn [ch]
                                 (swap! raw-data str ch)))
      (.on unzip-stream "end" (fn []
                                (let [parsed-data (js/JSON.parse @raw-data)]
                                  (handle-parsed-json
                                    (js->clj parsed-data :keywordize-keys true))))))))

(defn fetch-foli-data! []
  (.get http "http://data.foli.fi/siri/vm" handle-result))

(defn start-timer! []
  (register! :timer (js/setInterval fetch-foli-data! 5000)))

(defn create-server! []
  (let [app (e)
        server (.createServer http app)
        io (sio server)]
    (.on io "connection" (fn [socket]
                            (println "Client connected! Sending " (count @vehicles) "vehicles")
                            (.emit io "vehicles" (clj->js @vehicles))))
    (.listen server 3000 (fn []
                           (println "Server listening on port 3000")))
    (register! :websocket io)
    (register! :server server)))

(defn reload! []
  (stop-services!)
  (start-timer!)
  (create-server!))

(defn main! []
  (start-timer!)
  (create-server!))
