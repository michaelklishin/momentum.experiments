(ns com.novemberain.momentum.examples.server.http-proxy
  (:gen-class)
  (:require [clojure.tools.logging   :as logging]
            [momentum.http.client    :as http.client]
            [momentum.http.server    :as http.server]
            [momentum.http.aggregate :as aggregate])
  (:use [clojure.tools.cli]
        [momentum.core.buffer]))


;;
;; Implementation
;;

(defn exit!
  [s]
  (logging/error s)
  (System/exit 1))


(defn- initiate-request
  [dn evt headers body]
  (let [host (headers "host")
        path (:path-info headers)]
    (println (str "Handling a request for " host "(" path ")"))
    ))

(def ^:const default-options {:cycles 0 :scheme nil})

(defn- initiate-request
  [downstream state original-headers original-body]
  (let [host (original-headers "host")]
    (http.client/connect
     (fn [upstream _]
       (fn [evt val]
         (println "In the client: evt = " evt ", val = " val)
         (cond (= :open evt)
               (do
                 (logging/infof "Connected upstream (%s)" host)
                 (upstream :request [original-headers original-body]))

               (= :response evt)
               (do
                 (let [[status headers body] val]
                   (logging/infof "Upstream :response: %d, body = %s" status body)
                   (if (= :chunked body)
                     (do
                       (logging/infof "Got the response from upstream (%s): status = %d, body is chunked" host status))
                     (do
                       (logging/infof "Got the response from upstream (%s): status = %d, body = %s" host status (to-string body))
                       (downstream :response [status headers body])))))

               (= :body evt)
               (do
                 (logging/infof "Got a body chunk from upstream (%s)" host)
                 (downstream :body val))

               (= :abort evt)
               (do
                 (logging/infof "Abort event (%s), val is %s" host (str val))
                 (.printStackTrace val))

               :else (logging/infof "An unknown upstream (%s) event: %s" host evt))))
     {
      :host host
      :port 80
      })))

(defn- http-proxy-app
  ([] (http-proxy-app {}))
  ([opts]
     (aggregate/middleware
      (fn [downstream env]
        (let [state (atom {})]
          (fn [evt val]
            (println "In the server: evt = " evt ", val = " val)
            (cond
             (= :request evt)
             (let [[headers body] val]
               (initiate-request downstream state headers body))))))
      opts)))

(defn start-server
  [host port]
  (logging/infof "Starting an HTTP proxy server on %s:%d" host port)
  (http.server/start (http-proxy-app { :upstream false }) { :host host :port port }))


(defn start
  [host port]
  (start-server host port))


;;
;; CLI
;;

(defn -main
  [& args]
  (let [[options positional-args banner] (cli args
                                              ["--port" "Port to bind to"])]
    (if-let [p (:port options)]
      (start "127.0.0.1" (Long/valueOf p))
      (exit! "--port switch is required"))
    nil))
