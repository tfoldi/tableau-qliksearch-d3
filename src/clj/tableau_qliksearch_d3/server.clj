(ns tableau-qliksearch-d3.server
  (:require [clojure.java.io :as io]
            [compojure.core :refer [ANY GET PUT POST DELETE defroutes]]
            [compojure.route :refer [resources]]
            [ring.middleware.defaults :refer [wrap-defaults api-defaults]]
            [ring.middleware.gzip :refer [wrap-gzip]]
            [ring.middleware.logger :refer [wrap-with-logger]]
            [environ.core :refer [env]]
<<<<<<< HEAD
            [org.httpkit.server :refer [run-server]])
=======
            [ring.adapter.jetty :refer [run-jetty]])
>>>>>>> cebdd990e50d92d8214f90d4bc76acfbd33833f2
  (:gen-class))

(defroutes routes
  (GET "/" _
    {:status 200
     :headers {"Content-Type" "text/html; charset=utf-8"}
     :body (io/input-stream (io/resource "public/index.html"))})
  (resources "/"))

(def http-handler
  (-> routes
      (wrap-defaults api-defaults)
      wrap-with-logger
      wrap-gzip))

(defn -main [& [port]]
  (let [port (Integer. (or port (env :port) 10555))]
<<<<<<< HEAD
    (run-server http-handler {:port port :join? false})))
=======
    (run-jetty http-handler {:port port :join? false})))
>>>>>>> cebdd990e50d92d8214f90d4bc76acfbd33833f2
