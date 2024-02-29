(ns js2clj2js.clj-ish)

(defn- bigmac-etc->feature [{:keys [bigmac-index country-name points]}]
  (let [{polygon-type :type open-coordinates :coordinates} points
        {:keys [dollar_price local_price]} bigmac-index
        close-polygon (fn [coords]
                        [(conj coords (first coords))])
        coordinates (if (= "polygon" polygon-type)
                      (close-polygon open-coordinates)
                      (mapv (fn [coords]
                              (close-polygon coords))
                            open-coordinates))]
    {:type "Feature"
     :geometry {:type (if (= "polygon" polygon-type)
                        "Polygon"
                        "MultiPolygon")
                :coordinates coordinates}
     :properties (assoc bigmac-index
                        :country-name country-name
                        :dollar-price (js/Number dollar_price)
                        :local-price (js/Number local_price))}))

(defn ->geo-json [data]
  (let [features (mapv bigmac-etc->feature data)]
    {:type "FeatureCollection"
     :features features}))