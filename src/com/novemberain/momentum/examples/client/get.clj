(ns com.novemberain.momentum.examples.client.get
  (:refer-clojure :exclude [get])
  (:gen-class)
  (:require [clojure.tools.logging   :as logging]
            [momentum.http.client    :as http.client]
            [momentum.http.aggregate :as aggregate])
  (:use [clojure.tools.cli]
        [momentum.core.buffer])
  (:import [java.net URI URL]))


;;
;; Implementation
;;

(def ^:const user-agent "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_7_2) AppleWebKit/534.52.7 (KHTML, like Gecko) Version/5.1.2 Safari/534.52.7")

(defn exit!
  [s]
  (logging/error s)
  (System/exit 1))

(defn inspect-abort-event
  [val]
  (.printStackTrace val))


(defn low-level-api-fetch
  [url]
  (let [u (URI. url)
        host    (.getHost u)
        headers {
                 :host         host
                 "host"        host
                 :request-method "GET"
                 :port         80
                 :path-info    (.getPath u)
                 :script-name  ""
                 :query-string (or (.getQuery u) "")}]
    (http.client/connect (fn [downstream _]
                           (fn [evt val]
                             (cond
                              (= :open     evt) (downstream :request [headers nil])

                              (= :response evt) (do
                                                  (let [[status headers body] val]
                                                    (logging/infof "Response (%d) from %s" status (.getHost u))
                                                    (logging/infof "Headers:\n\n%s\n\n" (str headers))))
                              (= :body evt)
                              (if val
                                (logging/infof "Got a body chunk: %s" (to-string val)))

                              ;; (#{:pause :resume} evt)
                              ;; (when-let [upstream @up]
                              ;;   (upstream evt val))


                              (= :abort evt)
                              (inspect-abort-event val)

                              :else
                              (when-not (#{:done} evt)
                                (println "Unhandled event " [evt val])))))
                         {
                          :host (.getHost u)
                          :port 80
                          })))


(defn start
  [url]
  (low-level-api-fetch url))


;;
;; CLI
;;

(defn -main
  [& args]
  (let [[options positional-args banner] (cli args)]
    (apply start positional-args))
  nil)
