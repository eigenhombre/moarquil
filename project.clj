(defproject moarquil "0.1.0-SNAPSHOT"
  :description ""
  :url "https://github.com/eigenhombre/moarquil"
  :dependencies [[org.clojure/clojure "1.7.0"]
                 [quil "2.2.6"]
                 [com.stuartsierra/component "0.3.0"]
                 [org.clojure/tools.namespace "0.2.11"]]
  :aliases {"autotest" ["spec" "-a"]}
  :plugins [[speclj "3.3.0"]]
  :test-paths ["spec"]
  :profiles {:dev {:source-paths ["dev"]
                   :dependencies [[speclj "3.3.0"]]}
             :uberjar {:aot :all}})
