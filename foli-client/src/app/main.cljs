(ns app.main
  (:require ["socket.io-client" :as sio]))

(defonce io (sio "http://localhost:3000"))
(defonce map-inst (atom nil))
(defonce vehicle-list (atom {}))

(.on io "connect" (fn []
                     (js/console.log "I am now connected!")))
(.on io "vehicles" (fn [vehicles]
                     (doall
                       (for [vehicle (js->clj vehicles :keywordize-keys true)
                           :let [id (:vehicle-id vehicle)
                                 latitude (:latitude vehicle)
                                 longitude (:longitude vehicle)
                                 marker (js/L.marker #js [latitude longitude])
                                 vehicle (get @vehicle-list id)]]
                         (if vehicle
                           (.setLatLng vehicle (js/L.LatLng. latitude longitude))
                           (do
                             (.addTo marker @map-inst)
                             (swap! vehicle-list assoc id marker)))))))

(def cartodb-positron (.tileLayer js/L
                        "https://{s}.basemaps.cartocdn.com/light_all/{z}/{x}/{y}{r}.png"
                        #js {
                          :attribution "&copy; <a href=\"https://www.openstreetmap.org/copyright\">OpenStreetMap</a> contributors &copy; <a href=\"https://carto.com/attributions\">CARTO</a>"
                          :subdomains "abcd"
                          :maxZoom 19}))


(defn init-map! []
  (when (nil? @map-inst)
    (let [leaflet (js/L.map "map")]
      (.setView leaflet (clj->js [60.4321284 22.0841281]) 13)
      (.addTo cartodb-positron leaflet)
      (reset! map-inst leaflet))))

(defn main! []
  (init-map!))

(defn reload! [])
