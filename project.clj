(defproject com.novemberain/momentum.examples "1.0.0-SNAPSHOT"
  :description "A collection of examples for Momentum, a Clojure library for all things async"
  :dependencies [[org.clojure/clojure        "1.3.0"]
                 [org.clojure/tools.logging  "0.2.3" :exclude [org.clojure/clojure]]
                 [org.slf4j/slf4j-simple     "1.6.2"]
                 [org.slf4j/slf4j-api        "1.6.2"]
                 [org.clojure/tools.cli      "0.2.1" :exclude [org.clojure/clojure]]
                 [org.clojure/data.json      "0.1.2" :exclude [org.clojure/clojure]]
                 [org.jboss.netty/netty      "3.2.7.Final"]
                 [io.tilde.momentum/momentum "0.3.0-SNAPSHOT"]]
  :warn-on-reflection true
  :run-aliases {
                :hello-world-server com.novemberain.momentum.examples.server.hello-world
                :echo-server        com.novemberain.momentum.examples.server.echo
                :tracer-server      com.novemberain.momentum.examples.server.tracer
                :http-proxy-server  com.novemberain.momentum.examples.server.http-proxy

                :get-client         com.novemberain.momentum.examples.client.get
                })
