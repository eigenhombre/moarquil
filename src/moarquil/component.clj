(ns moarquil.component
  (:require [com.stuartsierra.component :as component]))


(defrecord Applet [applet]
  component/Lifecycle
  (start [component]
    (if applet
      component
      (assoc component :applet applet)))
  (stop [component]
    (if-not applet
      component
      (do
        (.exit applet)
        (assoc component :applet nil)))))


(defn new-system []
  (-> (component/system-map
       :applet (Applet. (quil.applet/applet)))))
