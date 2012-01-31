(ns com.novemberain.momentum.examples.server.hello-world
  (:gen-class)
  (:require [clojure.tools.logging   :as logging]
            [momentum.http.server    :as http.server])
  (:use [clojure.tools.cli]
        [momentum.core.buffer]))


;;
;; Implementation
;;

(defn exit!
  [s]
  (logging/error s)
  (System/exit 1))


(defn- respond-to
  [dn headers body]
  (dn :response [200 {:http-version [1 0]
                      :content-type "text/plain"} (buffer "Hello")]))


(defn- request-handler
  [dn _]
  (fn [evt [headers body]]
    (case evt
      :request (respond-to dn headers body)
      :done    _)))

(defn start-server
  [host port]
  (logging/infof "Starting a server on %s:%d" host port)
  (http.server/start request-handler { :host host :port port }))


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