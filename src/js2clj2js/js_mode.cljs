(ns js2clj2js.js-mode
  (:require [applied-science.js-interop :as j]
            [applied-science.js-interop.alpha :as j-alpha]))

(j-alpha/js
 (defn- bigmac-etc->feature [{:keys [bigmac-index country-name points]}]
   (let [{polygon-type :type open-coordinates :coordinates} points
         {:keys [dollar_price local_price]
          :or {dollar_price 0
               local_price 0}} bigmac-index
         close-polygon (fn [coords]
                         [(j/push! coords (first coords))])
         coordinates (if (= "polygon" polygon-type)
                       (close-polygon open-coordinates)
                       (map (fn [coords]
                              (close-polygon coords))
                            open-coordinates))]
     {:type "Feature"
      :geometry {:type (if (= "polygon" polygon-type)
                         "Polygon"
                         "MultiPolygon")
                 :coordinates (into-array coordinates)}
      :properties (j/assoc! bigmac-index
                            :country-name country-name
                            :dollar-price (js/Number dollar_price)
                            :local-price (js/Number local_price))}))

 (defn ->geo-json [data]
   (let [features (map bigmac-etc->feature data)]
     {:type "FeatureCollection"
      :features (into-array features)})))
