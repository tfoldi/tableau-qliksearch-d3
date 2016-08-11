(ns tableau-qliksearch-d3.container
  (:require [reagent.core :as reagent]
            [clojure.walk :refer [walk]]
            [cemerick.url :as url]
            [taoensso.timbre :as timbre :refer-macros (tracef debugf infof warnf errorf)]
            [tableau-qliksearch-d3.comm :as comm]
            [tableau-qliksearch-d3.state :as state :refer [app-state]]))

(def viz-url "http://public.tableau.com/views/TableauD3QlikSearch/ShowReel")

(def viz-url-with-parameters
  (let [url-for (fn [controller]
                  (-> state/location 
                      (assoc :query {:uuid state/uniq-id :controller controller})
                      str))]
    (-> (url/url viz-url)
        (assoc :query 
               {"Search URL" (url-for "search") ; more to come
                "D3 URL" (url-for "d3")}) 
        str)))

(defn embed-page[]
  [:div
   [:h1 "Embed D3 to Tableau Viz with " [:code "getData()"]  ]
   [:div#viz-container]])


(defn get-sheet-in-active-sheet
  "Get the `Sheet` object from the active sheet. Active sheet must be a `Dashboard`"
  [sheet]
  (-> (:vizobj @app-state)
      (.getWorkbook)
      (.getActiveSheet)
      (.getWorksheets)
      (.get sheet)))

(defn get-summary!
  "Get summary data from `Category` sheet and store in state container"
  []
  (-> (get-sheet-in-active-sheet "Category")
      (.getSummaryDataAsync (clj->js {:maxRows 1000}))
      (.then (fn [data]
               (let [cols (.getColumns data) rows (js->clj (.getData data) :keywordize-keys true )]
               (swap! app-state assoc 
                      :summary-columns (map  #(.getFieldName %) cols)
                      :summary-data (walk #(mapv :value %) vec rows)))))))


(when (= "container" state/controller)
  (state/when-change :tableau/get-summary-data
                     (fn []
                       (swap! app-state dissoc :tableau/get-summary-data)
                       (comm/chsk-send! 
                         [:tableau/summary-data 
                          (merge 
                            (select-keys @app-state [:summary-data :summary-columns])
                            {:origin (str state/uniq-id "/d3")})]))))


(def viz-options
  (js-obj
    "hideTabs" true
    "hideToolbar" true
    "width" "800px"
    "height" "600px"
    "onFirstInteractive" (fn [] 
                           (swap! app-state assoc :ready true) 
                           (get-summary!))))

(defn init-tableau! []
  (.getScript js/$ "https://public.tableau.com/javascripts/api/tableau-2.1.0.js"
              #(swap! app-state assoc :ready false :vizobj
                      (js/tableau.Viz. (.getElementById js/document "viz-container") 
                                       viz-url-with-parameters
                                       viz-options))))

(defn app []
  (reagent/create-class {:reagent-render embed-page
                         :component-did-mount init-tableau!}))


