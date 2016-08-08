(ns tableau-qliksearch-d3.core
  (:require [reagent.core :as reagent]
            [cljsjs.bootstrap]
            [cljsjs.jquery]
            [tableau-qliksearch-d3.state :as state :refer [app-state]]
            [tableau-qliksearch-d3.container :as container]
            [tableau-qliksearch-d3.comm]
            [taoensso.timbre :as timbre :refer-macros (tracef debugf infof warnf errorf)]))

(enable-console-print!)


(def controller-lookup {"container" container/app})


(reagent/render [(get controller-lookup state/controller)] 
                (js/document.getElementById "app"))
