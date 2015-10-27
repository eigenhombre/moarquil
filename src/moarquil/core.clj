(ns moarquil.core
  (:require [quil.core :refer :all]
            [quil.helpers.drawing :refer [line-join-points]]
            [quil.helpers.calc :refer [mul-add]]))



(defonce app (atom nil))


;; http://stackoverflow.com/questions/12545570/\
;; how-to-destroy-processing-papplet-without-calling-exit
;;(.destroy foo) ;; Sometimes kills REPL process
(when @app
  (.setVisible (.frame @app) false)
  (reset! app nil))


(def radius 400)


(defn setup []
  (background 255)
  (stroke 00))


(defn ^:private lissajeux-line []
  (for [t (map (partial * 0.05) (range 0 3610))]
    (let [s (* t 100)
          radian-s (radians s)
          radian-t (radians t)
          x (* radius  (cos radian-s) (sin radian-t))
          y (* radius  (sin radian-s) (sin radian-t))
          z (* radius (cos radian-t))]
      [x y z])))


(defn- render [lines]
  (doseq [l lines]
    (dorun
     (map (partial apply line) (line-join-points l)))))


(defn draw []
  (background 250)
  (translate (/ (width) 2) (/ (height) 2) 0)
  (rotate-y (* (frame-count) 0.0005))
  (rotate-x (* (frame-count) 0.0025))
  (render [(lissajeux-line)]))


(let [thisapp (quil.applet/applet
               :size [840 1200]
               :setup setup
               :draw draw
               :renderer :opengl)]
  (.setLocation (.frame thisapp) 0 0)
  (reset! app thisapp))
