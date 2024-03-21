(ns js2clj2js.main
  (:require [cognitect.transit :as transit]
            [cljs-bean.core :as bean]
            [promesa.core :as p]
            [js2clj2js.clj-data :as clj-data]
            [js2clj2js.clj-data-transit :as clj-data-transit]
            [js2clj2js.js-data :as js-data]
            [js2clj2js.js-mode :as js-mode]
            [js2clj2js.js-interop :as js-interop]
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

(defn do-x-times [x f & args]
  (first (mapv (fn [_]
                 (apply f args))
               (range x))))

(def transform-runs 1)

(defn ^:export js2clj2js []
  (world-map/set-data! world-map/empty-geojson)
  (timer-init! :js2clj2js)
  (-> (p/let [response (js/fetch "countries-w-polygons-and-bigmacs.json")
              _ (t-log! :js2clj2js :fetch)
              json-input (.json response)
              _ (t-log! :js2clj2js :response->json)
              clj-input (js->clj json-input :keywordize-keys true)
              _ (t-log! :js2clj2js :js->clj)
              clj-polygons (do-x-times transform-runs clj-data/->geo-json clj-input)
              _ (t-log! :js2clj2js :transform)
              js-polygons (clj->js clj-polygons)
              _ (t-log! :js2clj2js :clj->js)]
        (t-log! :js2clj2js :total)
        (js/console.table (clj->js (get-in @!timers [:js2clj2js :log])))
        (js/console.debug "Total ms: :js2clj2js" (get-in @!timers [:js2clj2js :total]))

        (world-map/set-data! js-polygons)
        js-polygons)
      (p/catch js/console.error)))

(defn ^:export bean-js2clj2js []
  (world-map/set-data! world-map/empty-geojson)
  (timer-init! :bean-js2clj2js)
  (-> (p/let [response (js/fetch "countries-w-polygons-and-bigmacs.json")
              _ (t-log! :bean-js2clj2js :fetch)
              json-input (.json response)
              _ (t-log! :bean-js2clj2js :response->json)
              clj-input (bean/->clj json-input :keywordize-keys true)
              _ (t-log! :bean-js2clj2js :bean->clj)
              clj-polygons (do-x-times transform-runs clj-data/->geo-json clj-input)
              _ (t-log! :bean-js2clj2js :transform)
              js-polygons (bean/->js clj-polygons)
              _ (t-log! :bean-js2clj2js :bean->js)]
        (t-log! :bean-js2clj2js :total)
        (js/console.table (clj->js (get-in @!timers [:bean-js2clj2js :log])))
        (js/console.debug "Total ms: :bean-js2clj2js" (get-in @!timers [:bean-js2clj2js :total]))

        (world-map/set-data! js-polygons)
        js-polygons)
      (p/catch js/console.error)))

(def reader (transit/reader :json))

(defn ^:export transit-js2clj2js []
  (world-map/set-data! world-map/empty-geojson)
  (timer-init! :transit-js2clj2js)
  (-> (p/let [response (js/fetch "countries-w-polygons-and-bigmacs.json")
              _ (t-log! :transit-js2clj2js :fetch)
              json-string (-> response .text)
              _ (t-log! :transit-js2clj2js :response->string)
              clj-input (transit/read reader json-string)
              _ (t-log! :transit-js2clj2js :transit-json->clj)
              clj-polygons (do-x-times transform-runs clj-data-transit/->geo-json clj-input)
              _ (t-log! :transit-js2clj2js :transform)
              js-polygons (clj->js clj-polygons)
              _ (t-log! :transit-js2clj2js :clj->js)]
        (t-log! :transit-js2clj2js :total)
        (js/console.table (clj->js (get-in @!timers [:transit-js2clj2js :log])))
        (js/console.debug "Total ms: :transit-js2clj2js" (get-in @!timers [:transit-js2clj2js :total]))

        (world-map/set-data! js-polygons)
        js-polygons)
      (p/catch js/console.error)))

(defn ^:export as-jsi []
  (world-map/set-data! world-map/empty-geojson)
  (timer-init! :as-jsi)
  (-> (p/let [response (js/fetch "countries-w-polygons-and-bigmacs.json")
              _ (t-log! :as-jsi :fetch)
              json-input (.json response)
              _ (t-log! :as-jsi :response->json)
              js-polygons (do-x-times transform-runs js-data/->geo-json json-input)
              _ (t-log! :as-jsi :transform)]
        (t-log! :as-jsi :total)
        (js/console.table (clj->js (get-in @!timers [:as-jsi :log])))
        (js/console.debug "Total ms: :as-jsi" (get-in @!timers [:as-jsi :total]))

        (world-map/set-data! js-polygons)
        js-polygons)
      (p/catch js/console.error)))


(defn ^:export js-mode-as-jsi []
  (world-map/set-data! world-map/empty-geojson)
  (timer-init! :js-mode)
  (-> (p/let [response (js/fetch "countries-w-polygons-and-bigmacs.json")
              _ (t-log! :js-mode :fetch)
              json-input (.json response)
              _ (t-log! :js-mode :response->json)
              js-polygons (do-x-times transform-runs js-mode/->geo-json json-input)
              _ (t-log! :js-mode :transform)]
        (t-log! :js-mode :total)
        (js/console.table (clj->js (get-in @!timers [:js-mode :log])))
        (js/console.debug "Total ms: :js-mode" (get-in @!timers [:js-mode :total]))

        (world-map/set-data! js-polygons)
        js-polygons)

      (p/catch js/console.error)))

(defn ^:export js-interop []
  (world-map/set-data! world-map/empty-geojson)
  (timer-init! :js-interop)
  (-> (p/let [response (js/fetch "countries-w-polygons-and-bigmacs.json")
              _ (t-log! :js-interop :fetch)
              json-input (.json response)
              _ (t-log! :js-interop :response->json)
              js-polygons (do-x-times transform-runs js-interop/->geo-json json-input)]
        (t-log! :js-interop :transform)
        (t-log! :js-interop :total)

        (js/console.table (clj->js (get-in @!timers [:js-interop :log])))
        (js/console.debug "Total ms: :js2clj2js" (get-in @!timers [:js-interop :total]))

        (world-map/set-data! js-polygons)
        js-polygons)

      (p/catch js/console.error)))

(comment
  (transit-js2clj2js)

  (p/let [js2clj2js-data (js2clj2js)
          as-jsi-data (as-jsi)
          js-mode-as-jsi-data (js-mode-as-jsi)
          bean-js2clj2js-data (bean-js2clj2js)
          clj-data (mapv js->clj [js2clj2js-data as-jsi-data js-mode-as-jsi-data bean-js2clj2js-data])
          equality (apply = clj-data)]
    (tap> clj-data)
    (def js2clj2js-data js2clj2js-data)
    (def clj-data clj-data)
    (def equality equality)
    (println equality))

  (p/let [data (js-interop)
          data (js->clj data)]
    (js/console.log "BOOM!" (first data))
    (def data data)
    (count (data "features"))
    (tap> data))
  :rcf)

;; Note, this function is not rebound on code reload
(defn ^:export key-down [e]
  (case (-> e .-code)
    "Digit1" (js2clj2js)
    "Digit2" (as-jsi)
    "Digit3" (js-mode-as-jsi)
    "Digit4" (bean-js2clj2js)
    "Digit5" (transit-js2clj2js)
    "Digit6" (js-interop)
    :nop))

(defn ^:after-load rerender! []
  (let [replace (js/document.getElementById "replace")]
    (set! (.-innerHTML replace) "")))

(defn ^:export init! []
  (rerender!)
  (world-map/create-map!))