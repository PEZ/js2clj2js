(ns js2clj2js.js-data-embrace-interop)

;; From @thheller on Clojurians Slack:
;; https://clojurians.slack.com/archives/C8NUSGWG6/p1710932480031979?thread_ts=1710929807.032009&cid=C8NUSGWG6

(defn close-polygon [coords]
  (.push coords (aget coords 0))
  coords)

(defn bigmac-index [json-data]
  (when-some [bigmac-index (unchecked-get json-data "bigmac-index")]
    (let [dollar-price (unchecked-get bigmac-index "dollar-price")
          local-price (unchecked-get bigmac-index "local-price")]
      ;; FIXME: dunno how much of this is actually used
      ;; could skip js->cljs and just get the parts needed manually
      (-> (js->clj json-data)
          (assoc :dollar-price (js/Number dollar-price))
          (assoc :local-price (js/Number local-price))))))

(defn- bigmac-etc->feature [json-data]
  (let [country-name (unchecked-get json-data "country-name")
        points (unchecked-get json-data "points")
        polygon-type (unchecked-get points "type")
        open-coordinates (unchecked-get points "coordinates")
        coordinates
        (if (= "polygon" polygon-type)
          (close-polygon open-coordinates)
          (.map open-coordinates close-polygon))]

    {:type "Feature"
     :geometry
     {:type (if (= "polygon" polygon-type) "Polygon" "MultiPolygon")
      :coordinates coordinates}
     :properties
     (assoc (bigmac-index json-data)
            :country-name country-name)}))

(defn ->geo-json [data]
  (let [features (mapv bigmac-etc->feature data)]
    #js {:type "FeatureCollection"
         :features (into-array features)}))
