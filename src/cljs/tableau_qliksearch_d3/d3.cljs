(ns tableau-qliksearch-d3.d3
  (:require [reagent.core :as reagent]
            [cljsjs.d3]
            [taoensso.timbre :as timbre :refer-macros (tracef debugf infof warnf errorf)]
            [taoensso.sente  :as sente  :refer (cb-success?)] 
            [tableau-qliksearch-d3.comm :as comm]
            [tableau-qliksearch-d3.state :as state :refer [app-state]]))

(defn init-load-page []
  [:div 
   [:link {:href "http://cdnjs.cloudflare.com/ajax/libs/font-awesome/4.6.3/css/font-awesome.min.css"
           :rel "stylesheet"}]
   [:h2
    [:i.fa.fa-circle-o-notch.fa-spin]
    " Loading Data to D3 viz"]])

(defn receive-summary-data [data]
  (debugf "summary data arrived: %s" data)
  (swap! app-state assoc :state :data-loaded)
  (.getScript js/$ "/reel.js" (swap! app-state assoc :state :d3-loaded) ))

(defn request-summary-data []
  ; retry every 3s if no response
  (js/setTimeout #(when (nil? (:state @app-state)) (request-summary-data)) 3000 )
  (comm/chsk-send! [:tableau/get-summary-data {:origin (str state/uniq-id "/container")}] ))

(defn transpose [m]
  (apply mapv vector m))

(defn ^:export summary-data []
  (let [sumdata (get-in @app-state [:tableau/summary-data 1])
        transpose (fn [m] (apply mapv vector m))
        data (:summary-data sumdata)
        cols (:summary-columns sumdata)]
    (debugf "sum data: %s" sumdata)
    (clj->js (mapv #(zipmap ["date" "category" "year" "sales"] %) data))))

(when (= "d3" state/controller)
  (state/when-change :chsk/handshake request-summary-data)
  (state/when-change :tableau/summary-data receive-summary-data))

(defn app[]
  (condp = (:state @app-state)
    :d3-loaded nil
    :data-loaded [:h1 "Loading D3 viz..."]
    (init-load-page)))
