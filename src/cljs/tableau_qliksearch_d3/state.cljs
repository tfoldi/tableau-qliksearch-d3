(ns tableau-qliksearch-d3.state
  (:require [reagent.core :as reagent :refer [atom]]
            [cemerick.url :as url]
            [taoensso.encore :as enc]))

; app const - things never change
(def location (cemerick.url/url (-> js/window .-location .-href)))

(def query (:query location))

(def controller 
  (or (get query "controller") "container"))

(def uniq-id
  (or (get query "uuid") (enc/uuid-str)))

(def uid (str uniq-id "/" controller))


; app state - things that can change
(defonce app-state (atom {}))

(defn when-change [k block]
  (add-watch app-state k
             (fn [_ _ old-state new-state]
               (when (and (contains? new-state k)
                          (not (contains? old-state k)))
                 (block new-state)))))


