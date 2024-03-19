(ns js2clj2js.world-map
  (:require [hiccups.runtime])
  (:require-macros [hiccups.core :as hiccups :refer [html]]))

(defonce !map-instance (atom nil))

(def layer-id "countries-layer")
(def source-id "countries-source")

(defn- remove-popups []
  (doseq [popup (js/document.getElementsByClassName "maplibregl-popup")]
    (.remove popup)))

(defn set-data! [^js data]
  (.setData (.getSource ^js @!map-instance source-id) data))

(def min-dollar-price 1.5)
(def max-dollar-price 8.0)

(def empty-geojson #js {:type "FeatureCollection"
                        :features #js []})

(defn- add-countries-layer! [^js map-instance]
  (let [on-hover (fn on-hover [^js e]
                   (let [^js features (.queryRenderedFeatures map-instance (.-point e) (clj->js {:layers [layer-id]}))
                         ^js first-feature (first features)
                         properties (js->clj (.-properties first-feature) :keywordize-keys true)
                         {:keys [country-name dollar-price local-price currency_code]} properties
                         round-fn (fn [p] (-> p (* 1000) int (/ 1000)))
                         hiccup [:div
                                 [:h3 country-name]
                                 (if (< 0 dollar-price)
                                   [:div
                                    [:p [:strong "Big Mac price:"]]
                                    [:p (round-fn dollar-price) " USD"]
                                    [:p (round-fn local-price) " " currency_code]]
                                   [:p "(No Big Mac Index here)"])]]
                     (remove-popups)
                     (doto ^js (new (.-Popup js/maplibregl) #js {:offset 25
                                                                 :closeButton false})
                       (.setLngLat (.-lngLat e))
                       (.setHTML (html hiccup))
                       (.addTo map-instance))))]
    (.addSource map-instance source-id #js {:type "geojson"
                                            :data empty-geojson})
    (.addLayer map-instance (clj->js {:id layer-id
                                      :type "fill"
                                      :source source-id
                                      :paint {:fill-color ["interpolate",
                                                           ["linear"],
                                                           ["number" ["get", "dollar-price"]],
                                                           1 "#FFFFFF"
                                                           min-dollar-price, "hsl(120, 100%, 50%)",
                                                           (/ (+ min-dollar-price max-dollar-price) 2), "hsl(60, 100%, 50%)",
                                                           max-dollar-price, "hsl(0, 100%, 50%)"],
                                              :fill-opacity 1.0}}))
    (.on map-instance "mousemove" layer-id (fn [e] (on-hover e)))
    (.on map-instance "click" layer-id (fn [e] (on-hover e)))
    (.on map-instance "mouseleave" layer-id remove-popups)))

(defn add-geolocate-control! [^js map-instance]
  (let [geolocate-control ^js (new (.-GeolocateControl js/maplibregl)
                                   #js {:positionOptions #js {:enableHighAccuracy true}
                                        :trackUserLocation true
                                        :fitBoundsOptions #js {:maxZoom 11}})
        navigation-control ^js (new (.-NavigationControl js/maplibregl) #js {:showCompass true})]
    (.addControl map-instance navigation-control "bottom-left")
    (.addControl map-instance geolocate-control "bottom-left")
    geolocate-control))

(defn create-map! []
  (let [map-instance ^js (new (.-Map js/maplibregl) #js {:container "map"
                                                         :style "https://demotiles.maplibre.org/style.json"})]
    (reset! !map-instance map-instance)
    (.on map-instance "load" (fn [_e]
                               (add-countries-layer! map-instance)
                               (add-geolocate-control! map-instance)))
    (.on map-instance "error" (fn [error]
                                (throw error)))))
