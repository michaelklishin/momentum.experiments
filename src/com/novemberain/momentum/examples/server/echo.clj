(ns com.novemberain.momentum.examples.server.echo
  (:gen-class)
  (:require [clojure.tools.logging   :as logging]
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


(defn- echo-app
  ([] (echo-app {}))
  ([opts]
     (aggregate/middleware
      (fn [dn env]
        (fn [evt [headers body]]
          (cond
           (= :request evt)
           (dn :response [200 headers body])

           :else
           (dn evt [headers body]))))
      opts)))

(defn start-server
  [host port]
  (logging/infof "Starting a server on %s:%d" host port)
  (http.server/start (echo-app) { :host host :port port }))


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
