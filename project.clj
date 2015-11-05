(defproject moarquil "0.1.0-SNAPSHOT"
  :description ""
  :main moarquil.core
  :aot [moarquil.core]
  :url "https://github.com/eigenhombre/moarquil"
  :dependencies [[org.clojure/clojure "1.7.0"]
                 [quil "2.3.0-SNAPSHOT"]
                 [eigenhombre/namejen "0.1.11"]
                 [net.mikera/core.matrix "0.43.0"]]
  :profiles {:uberjar {:aot :all}})
