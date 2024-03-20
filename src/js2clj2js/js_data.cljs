(ns js2clj2js.js-data
  (:require [applied-science.js-interop :as j]))

(j/defn ^:private bigmac-etc->feature [^js {:keys [bigmac-index country-name points]}]
  (j/let [^js {polygon-type :type open-coordinates :coordinates} points
          ^js {:keys [dollar_price local_price]
               :or {dollar_price 0
                    local_price 0}} bigmac-index
          close-polygon (j/fn [^js coords]
                          #js [(j/push! coords (first coords))])
          coordinates (if (= "polygon" polygon-type)
                        (close-polygon open-coordinates)
                        (map (j/fn [^js coords]
                               (close-polygon coords))
                             open-coordinates))]
    #js {:type "Feature"
         :geometry #js {:type (if (= "polygon" polygon-type)
                                "Polygon"
                                "MultiPolygon")
                        :coordinates (into-array coordinates)}
         :properties (j/assoc! bigmac-index
                               :country-name country-name
                               :dollar-price (js/Number dollar_price)
                               :local-price (js/Number local_price))}))

(defn ->geo-json [data]
  (let [features (map bigmac-etc->feature data)]
    #js {:type "FeatureCollection"
         :features (into-array features)}))
