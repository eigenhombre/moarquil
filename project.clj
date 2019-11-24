(defproject moarquil "0.1.0-SNAPSHOT"
  :description ""
  :main moarquil.core
  :url "https://github.com/eigenhombre/moarquil"
  :dependencies [[org.clojure/clojure "1.10.0"]
                 [quil "2.3.0-SNAPSHOT"]
                 [eigenhombre/namejen "0.1.11"]
                 [net.mikera/core.matrix "0.43.0"]]
  :profiles {:dev {:dependencies [[michaelblume/marginalia "0.9.0"]]
                   :plugins [[michaelblume/lein-marginalia "0.9.0"]]}
             :uberjar {:aot :all
                       :uberjar-name "moarquil.jar"}})
