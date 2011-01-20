(defproject phm "1.0.0-SNAPSHOT"
  :description "FIXME: write"
  :dev-dependencies [[swank-clojure "1.3.0-SNAPSHOT"]]
  :dependencies [[org.clojure/clojure "1.3.0-alpha4"]
                 [com.stuartsierra/lazytest "2.0.0-SNAPSHOT"]
                 [org.clojure/clojure-contrib "1.2.0"]
                 [compojure "0.5.2"]
                 [redis.clients/jedis "1.5.0"]
                 [org.clojars.technomancy/clj-stacktrace "0.2.1-SNAPSHOT"]]
  :repositories {"stuartsierra" "http://stuartsierra.com/m2snapshots"})
