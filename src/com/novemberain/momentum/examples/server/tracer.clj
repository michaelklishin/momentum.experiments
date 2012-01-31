(ns com.novemberain.momentum.examples.server.tracer
  (:gen-class)
  (:require [clojure.tools.logging   :as logging]
            [momentum.net.server     :as net.server])
  (:use [clojure.tools.cli]
        [momentum.core.buffer]))


;;
;; Implementation
;;

(defn exit!
  [s]
  (logging/error s)
  (System/exit 1))


(defn- tracer-app
  []
  (fn [dn env]
    (fn [evt val]
      (when (= :message evt)
        (println (str (to-string val) "\n\n\n"))))))

(defn start-server
  [host port]
  (logging/infof "Starting a server on %s:%d" host port)
  (net.server/start (tracer-app) { :host host :port port }))


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
