(ns moarquil.core
  (:gen-class)
  (:require [moarquil.geom :refer [content reset-content!]]
            [quil.core :refer :all]
            [quil.helpers.drawing :refer [line-join-points]]))


(defonce app (atom nil))


;; http://stackoverflow.com/questions/12545570/\
;; how-to-destroy-processing-papplet-without-calling-exit
;;(.destroy foo) ;; Sometimes kills REPL process
(when @app
  (.setVisible (.frame @app) false)
  (reset! app nil))


(defn setup []
  (fill 0)
  (smooth 4)
  (stroke 00))


(defn- draw-sphere [{:keys [value origin radius]}]
  (push-matrix)
  (push-style)
  (fill value)
  (no-stroke)
  (sphere-detail 15)
  (translate origin)
  (sphere radius)
  (pop-style)
  (pop-matrix))


(defn- draw-ring [{:keys [pos r1 r2 dr rotx color]}]
  (push-matrix)
  (apply translate pos)
  (rotate-x rotx)
  (push-style)
  (let [sides 60
        angle (/ (* Math/PI 2) sides)]
    (doseq [r (range r1 r2 dr)]
      (no-fill)
      (stroke-weight 2)
      (stroke color)
      (begin-shape)
      (doseq [i (range (inc sides))]
        (vertex (* r (Math/cos (* i angle)))
                (* r (Math/sin (* i angle)))
                0))
      (end-shape)))
  (pop-style)
  (pop-matrix))


(defn render [objects]
  (doseq [{type_ :type :as l} objects]
    (cond
      (= type_ :line)
      (doseq [p (:points l)]
        (stroke-weight (min 0.1 (- (/ (last p) 300))))
        (apply line p))

      (= type_ :text)
      (apply (partial text (:txt l))
             (:pos l))

      (= type_ :sphere)
      (draw-sphere l)

      (= type_ :ring)
      (draw-ring l))))


(defn draw []
  (background 250)
  (translate (/ (width) 2) (/ (height) 2) 0)
  (let [theta (* (+ 10000 (frame-count)) -0.0025)
        phi (* (frame-count) 0.0005)
        r 1000]
    (camera (* r (Math/cos phi) (Math/sin theta))
            (* r (Math/sin phi) (Math/sin theta))
            (* r (Math/cos theta))
            0 0 0
            0 1 1))
  (render (content)))


(defn key-press []
  (reset-content!))


(defn -main []
  (let [thisapp (quil.applet/applet
                 :size [950 1200]
                 :setup setup
                 :draw draw
                 :key-typed key-press
                 :renderer :p3d)]
    (.setLocation (.frame thisapp) 0 0)
    (reset! app thisapp)))
