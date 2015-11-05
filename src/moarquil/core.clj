(ns moarquil.core
  (:gen-class)
  (:require [moarquil.render :refer :all]
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
                 :mouse-dragged mouse-dragged
                 :mouse-pressed mouse-pressed
                 :mouse-released mouse-released
                 :mouse-wheel mouse-wheel
                 :renderer :opengl)]
    (.setLocation (.frame thisapp) 0 0)
    (future (update-camera-positions-continuously))
    (reset! app thisapp)))
