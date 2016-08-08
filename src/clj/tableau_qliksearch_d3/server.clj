(ns tableau-qliksearch-d3.server
  (:require [clojure.java.io :as io]
            [compojure.core :refer [ANY GET PUT POST DELETE defroutes]]
            [compojure.route :refer [resources]]
            [ring.middleware.defaults :refer [wrap-defaults site-defaults]]
            [ring.middleware.gzip :refer [wrap-gzip]]
            [ring.middleware.logger :refer [wrap-with-logger]]
            [environ.core :refer [env]]
            [org.httpkit.server :refer [run-server]]
            [taoensso.timbre    :as timbre :refer (tracef debugf infof warnf errorf)]
            [taoensso.sente :as sente]
            [taoensso.sente.server-adapters.http-kit :refer (get-sch-adapter)]
               )
  (:gen-class))

(reset! sente/debug-mode?_ true) ; Uncomment for extra debug info

;;;; Define our Sente channel socket (chsk) server

(let [packer :edn ; Default packer, a good choice in most cases
      chsk-server
      (sente/make-channel-socket-server!
        (get-sch-adapter) 
        {:packer packer
         :user-id-fn  (fn [ring-req] 
                              (debugf "new uid from handshake %s" (get-in ring-req [:params :uid]))
                              (get-in ring-req [:params :uid]))})

      {:keys [ch-recv send-fn connected-uids
              ajax-post-fn ajax-get-or-ws-handshake-fn]}
      chsk-server]

  (def ring-ajax-post                ajax-post-fn)
  (def ring-ajax-get-or-ws-handshake ajax-get-or-ws-handshake-fn)
  (def ch-chsk                       ch-recv) ; ChannelSocket's receive channel
  (def chsk-send!                    send-fn) ; ChannelSocket's send API fn
  (def connected-uids                connected-uids) ; Watchable, read-only atom
  )

;; We can watch this atom for changes if we like
(add-watch connected-uids :connected-uids
  (fn [_ _ old new]
    (when (not= old new)
      (infof "Connected uids change: %s" new))))


(defroutes routes
  (GET "/" _
    {:status 200
     :headers {"Content-Type" "text/html; charset=utf-8"}
     :body (io/input-stream (io/resource "public/index.html"))})
  (GET  "/chsk"  ring-req (ring-ajax-get-or-ws-handshake ring-req))
  (POST "/chsk"  ring-req (ring-ajax-post                ring-req))

  (resources "/"))

(defn event-msg-handler
  "Wraps `-event-msg-handler` with logging, error catching, etc."
  [{:as ev-msg :keys [id ?data event]}]
  (infof "Unhandled event: %s data %s id %s" event ?data id )
  ;;(-event-msg-handler ev-msg) ; Handle event-msgs on a single thread
  ;; (future (-event-msg-handler ev-msg)) ; Handle event-msgs on a thread pool
  )

(def http-handler
  (-> routes
      (wrap-defaults site-defaults)
      wrap-with-logger
      wrap-gzip))

;;;; Sente event router (our `event-msg-handler` loop)

(defonce router_ (atom nil))
(defn  stop-router! [] (when-let [stop-fn @router_] (stop-fn)))
(defn start-router! []
  (stop-router!)
  (debugf "starting router")
  (reset! router_
    (sente/start-server-chsk-router! ch-chsk event-msg-handler)))

(defonce _start-once (start-router!))

(defn -main [& [port]]
  (let [port (Integer. (or port (env :port) 10555))]
    (run-server http-handler {:port port :join? false})))
