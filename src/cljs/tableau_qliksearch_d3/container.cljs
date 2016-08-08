(ns tableau-qliksearch-d3.container
  (:require [reagent.core :as reagent]
            [tableau-qliksearch-d3.state :as state :refer [app-state]]))


(def viz-url "http://public.tableau.com/views/TableauD3QlikSerch/Dashboard")

(defn embed-page[]
  [:div
   [:h1 "Embedding Tableau Viz"]
   [:div#viz-container]])

(def viz-options
  (js-obj
    "hideTabs" true
    "hideToolbar" true
    "width" "800px"
    "height" "600px"
    "onFirstInteractive" #(swap! app-state assoc :ready true)))

(defn init-tableau []
  (.getScript js/$ "https://public.tableau.com/javascripts/api/tableau-2.1.0.js"
              #(swap! app-state assoc :ready false :vizobj
                      (js/tableau.Viz. (.getElementById js/document "viz-container") 
                                       viz-url 
                                       viz-options))))

(defn app []
  (reagent/create-class {:reagent-render embed-page
                         :component-did-mount init-tableau}))


