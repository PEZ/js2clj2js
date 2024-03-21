(ns js2clj2js.js-interop)

(defn close-polygon [coords]
  (when (pos? (alength coords))
    (let [f (aget coords 0)]
      (.push coords f)))
  #js [coords])

(defn bigmac-index [json-data country-name]
  (if-some [bigmac-index (unchecked-get json-data "bigmac-index")]
    ;; FIXME: mixing _ and -, should probably clean that up?
    (let [dollar-price (unchecked-get bigmac-index "dollar_price")
          local-price (unchecked-get bigmac-index "local_price")]
      (doto bigmac-index
        (unchecked-set "country-name" country-name)
        (unchecked-set "dollar-price" (if-not dollar-price 0 (js/Number dollar-price)))
        (unchecked-set "local-price" (if-not local-price 0 (js/Number local-price)))))
    #js {:country-name country-name
         :dollar-price 0
         :local-price 0}))

(defn- bigmac-etc->feature [json-data]
  (let [country-name (unchecked-get json-data "country-name")

        geometry
        (if-some [points (unchecked-get json-data "points")]
          (let [polygon-type (unchecked-get points "type")
                coordinates (unchecked-get points "coordinates")
                coordinates
                (if (= "polygon" polygon-type)
                  (close-polygon coordinates)
                  (.map coordinates close-polygon))]

            #js {:type (if (= "polygon" polygon-type) "Polygon" "MultiPolygon")
                 :coordinates coordinates})

          #js {:type "Polygon"
               :coordinates #js []})]

    #js {:type "Feature"
         :geometry geometry
         :properties (bigmac-index json-data country-name)}))

(defn ->geo-json [json-data]
  #js {:type "FeatureCollection"
       :features (.map json-data bigmac-etc->feature)})