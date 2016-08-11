(ns tableau-qliksearch-d3.comm

  (:require
    [clojure.string  :as str]
    [tableau-qliksearch-d3.state :as state]
    [cljs.core.async :as async  :refer (<! >! put! chan)]
    [taoensso.encore :as encore :refer-macros (have have?)]
    [taoensso.timbre :as timbre :refer-macros (tracef debugf infof warnf errorf)]
    [taoensso.sente  :as sente  :refer (cb-success?)] )
  (:require-macros
    [cljs.core.async.macros :as asyncm :refer (go go-loop)]))


;;;; Define our Sente channel socket (chsk) client

(let [packer :edn ; Default packer, a good choice in most cases
      {:keys [chsk ch-recv send-fn state]}
      (sente/make-channel-socket-client!
        "/chsk" ; Must match server Ring routing URL
        {:type   :auto
         :params {:uid state/uid}
         :packer packer})]

  (def chsk       chsk)
  (def ch-chsk    ch-recv) ; ChannelSocket's receive channel
  (def chsk-send! send-fn) ; ChannelSocket's send API fn
  (def chsk-state state)   ; Watchable, read-only atom
  )  


(defn event-msg-handler
  "Handle incoming messages"
  [{:keys [id ?data ] [_ [tag]] :event}]
  (condp = id
    :chsk/state nil
    :chsk/handshake (swap! state/app-state assoc id ?data)
    (do (debugf "Incoming message id=%s tag=%s" id tag)
        (swap! state/app-state assoc tag ?data))))

;;;; Sente event router (our `event-msg-handler` loop)
(defonce router_ (atom nil))
(defn  stop-router! [] (when-let [stop-f @router_] (stop-f)))
(defn start-router! []
  (stop-router!)
  (reset! router_
    (sente/start-client-chsk-router!
      ch-chsk event-msg-handler)))

(defonce _start-once (start-router!))

