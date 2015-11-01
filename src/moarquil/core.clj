(ns moarquil.core
  (:gen-class)
  (:require [moarquil.render :refer [setup
                                     draw
                                     key-press
                                     update-camera-positions]]
            [quil.core :refer :all]))


(defonce app (atom nil))


;; Clear existing display (for REPLing):
;; http://stackoverflow.com/questions/12545570/\
;; how-to-destroy-processing-papplet-without-calling-exit
(when @app
  (.setVisible (.frame @app) false)
  (reset! app nil))


(defn -main []
  (let [thisapp (quil.applet/applet
                 :size [1600 1200]
                 :setup setup
                 :draw draw
                 :key-typed key-press
                 :renderer :opengl)]
    (.setLocation (.frame thisapp) 0 0)
    (future (update-camera-positions))
    (reset! app thisapp)))
