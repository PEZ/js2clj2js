(ns js2clj2js.main
  (:require [cognitect.transit :as transit]
            [cljs-bean.core :as bean]
            [promesa.core :as p]
            [js2clj2js.clj-data :as clj-data]
            [js2clj2js.clj-data-transit :as clj-data-transit]
            [js2clj2js.js-data :as js-data]
            [js2clj2js.js-mode :as js-mode]
            [js2clj2js.world-map :as world-map]))

(def !timers (atom {}))

(def timed #{:fetch :response->json :clj->js :js->clj :bean->js :bean->clj :transform
             :response->string :transit-json->clj})

(defn timer-init! [t-id]
  (let [t (js/performance.now)]
    (swap! !timers assoc-in [t-id :t] t)
    (js/console.debug "Timer" (str t-id) "initialized")))

(defn t-log! [t-id label]
  (let [t (js/performance.now)
        dt (- t (get-in @!timers [t-id :t]))]
    (swap! !timers assoc-in [t-id :t] t)
    (when-not (= :total label)
      (js/console.debug (str label) dt))
    (cond
      (timed label) (swap! !timers assoc-in [t-id :log label] dt)
      (= :total label) (let [total (->> (get-in @!timers [t-id :log])
                                        vals
                                        (apply +))]
                         (swap! !timers assoc-in [t-id :total] total)
                         (js/console.debug (str label) total)))))

(defn ^:export js2clj2js []
  (world-map/set-data! world-map/empty-geojson)
  (timer-init! :js2clj2js)
  (p/let [response (js/fetch "countries-w-polygons-and-bigmacs.json")
          _ (t-log! :js2clj2js :fetch)
          json-input (.json response)
          _ (t-log! :js2clj2js :response->json)
          clj-input (js->clj json-input :keywordize-keys true)
          _ (t-log! :js2clj2js :js->clj)
          clj-polygons (clj-data/->geo-json clj-input)
          _ (t-log! :js2clj2js :transform)
          js-polygons (clj->js clj-polygons)
          _ (t-log! :js2clj2js :clj->js)]
    (t-log! :js2clj2js :total)
    (js/console.table (clj->js (get-in  @!timers [:js2clj2js :log])))
    (js/console.debug "Total ms: :js2clj2js" (get-in @!timers [:js2clj2js :total]))

    (world-map/set-data! js-polygons)
    js-polygons))

(defn ^:export bean-js2clj2js []
  (world-map/set-data! world-map/empty-geojson)
  (timer-init! :bean-js2clj2js)
  (p/let [response (js/fetch "countries-w-polygons-and-bigmacs.json")
          _ (t-log! :bean-js2clj2js :fetch)
          json-input (.json response)
          _ (t-log! :bean-js2clj2js :response->json)
          clj-input (bean/->clj json-input :keywordize-keys true)
          _ (t-log! :bean-js2clj2js :bean->clj)
          clj-polygons (clj-data/->geo-json clj-input)
          _ (t-log! :bean-js2clj2js :transform)
          js-polygons (bean/->js clj-polygons)
          _ (t-log! :bean-js2clj2js :bean->js)]
    (t-log! :bean-js2clj2js :total)
    (js/console.table (clj->js (get-in  @!timers [:bean-js2clj2js :log])))
    (js/console.debug "Total ms: :bean-js2clj2js" (get-in @!timers [:bean-js2clj2js :total]))

    (world-map/set-data! js-polygons)
    js-polygons))

(def reader (transit/reader :json))

(defn ^:export transit-js2clj2js []
  (world-map/set-data! world-map/empty-geojson)
  (timer-init! :transit-js2clj2js)
  (p/let [response (js/fetch "countries-w-polygons-and-bigmacs.json")
          _ (t-log! :transit-js2clj2js :fetch)
          json-string (-> response .text)
          _ (t-log! :transit-js2clj2js :response->string)
          clj-input (transit/read reader json-string)
          _ (t-log! :transit-js2clj2js :transit-json->clj)
          clj-polygons (clj-data-transit/->geo-json clj-input)
          _ (t-log! :transit-js2clj2js :transform)
          js-polygons (clj->js clj-polygons)
          _ (t-log! :transit-js2clj2js :clj->js)]
    (t-log! :transit-js2clj2js :total)
    (js/console.table (clj->js (get-in  @!timers [:transit-js2clj2js :log])))
    (js/console.debug "Total ms: :transit-js2clj2js" (get-in @!timers [:transit-js2clj2js :total]))

    (world-map/set-data! js-polygons)
    js-polygons))

(defn ^:export js2js []
  (world-map/set-data! world-map/empty-geojson)
  (timer-init! :js2js)
  (p/let [response (js/fetch "countries-w-polygons-and-bigmacs.json")
          _ (t-log! :js2js :fetch)
          json-input (.json response)
          _ (t-log! :js2js :response->json)
          js-polygons (js-data/->geo-json json-input)
          _ (t-log! :js2js :transform)]
    (t-log! :js2js :total)
    (js/console.table (clj->js (get-in  @!timers [:js2js :log])))
    (js/console.debug "Total ms: :js2js" (get-in @!timers [:js2js :total]))

    (world-map/set-data! js-polygons)
    js-polygons))


(defn ^:export js-mode-js2js []
  (world-map/set-data! world-map/empty-geojson)
  (timer-init! :js-mode-js2js)
  (p/let [response (js/fetch "countries-w-polygons-and-bigmacs.json")
          _ (t-log! :js-mode-js2js :fetch)
          json-input (.json response)
          _ (t-log! :js-mode-js2js :response->json)
          js-polygons (js-mode/->geo-json json-input)
          _ (t-log! :js-mode-js2js :transform)]
    (t-log! :js-mode-js2js :total)
    (js/console.table (clj->js (get-in  @!timers [:js-mode-js2js :log])))
    (js/console.debug "Total ms: :js-mode-js2js" (get-in @!timers [:js-mode-js2js :total]))

    (world-map/set-data! js-polygons)
    js-polygons))

(comment
  (transit-js2clj2js)

  (p/let [js2clj2js-data (js2clj2js)
          js2js-data (js2js)
          js-mode-js2js-data (js-mode-js2js)
          bean-js2clj2js-data (bean-js2clj2js)
          clj-data (mapv js->clj [js2clj2js-data js2js-data js-mode-js2js-data bean-js2clj2js-data])
          equality (apply = clj-data)]
    (tap> clj-data)
    (def js2clj2js-data js2clj2js-data)
    (def clj-data clj-data)
    (def equality equality)
    (println equality))
  :rcf)

;; Note, this function is not rebound on code reload
(defn ^:export key-down [e]
  (case (-> e .-code)
    "Digit1" (js2clj2js)
    "Digit2" (js2js)
    "Digit3" (js-mode-js2js)
    "Digit4" (bean-js2clj2js)
    "Digit5" (transit-js2clj2js)
    :nop))

(defn ^:after-load rerender! []
  (let [replace (js/document.getElementById "replace")]
    (set! (.-innerHTML replace) "")))

(defn ^:export init! []
  (rerender!)
  (world-map/create-map!))