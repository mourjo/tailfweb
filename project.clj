(defproject tailfweb "0.1.0"
  :description "tailf over websockets"
  :url "http://mourjo.me/"
  :min-lein-version "2.0.0"
  :dependencies [[org.clojure/clojure "1.8.0"]
                 [compojure "1.6.0"]
                 [ring "1.6.3"]
                 [http-kit "2.2.0"]
                 [org.clojure/core.async "0.4.474"]
                 [amalloy/ring-buffer "1.2.1"]
                 [org.slf4j/slf4j-log4j12 "1.7.21"]
                 [org.clojure/tools.logging "0.4.0"]]
  :main tailfweb.handler
  :repl-options {:init-ns tailfweb.handler}
  :uberjar-name "tailfweb.jar"
  :aot [tailfweb.handler
        tailfweb.observer])
