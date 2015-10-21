(ns user
  (:require [clojure.tools.namespace.repl :refer [refresh]]
            [com.stuartsierra.component :as component]
            [moarquil.core :as core]))

(def system nil)


(defn init []
  (alter-var-root #'system
                  (constantly (core/new-system))))


(defn start []
  (alter-var-root #'system component/start))


(defn stop []
  (alter-var-root #'system (fn [s] (when s (component/stop s)))))


(defn go []
  (init)
  (start))


(defn reset []
  (stop)
  (refresh :after 'user/go))
