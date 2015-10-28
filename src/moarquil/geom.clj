(ns moarquil.geom
  (:require [namejen.names :refer [generic-name]]
            [quil.core :refer :all]
            [quil.helpers.drawing :refer [line-join-points]]))


(defn ^:private lissajeux-line [radius]
  (line-join-points
   (for [t (map (partial * 0.05) (range 0 3610))]
     (let [s (* t 100)
           radian-s (radians s)
           radian-t (radians t)
           x (* radius  (cos radian-s) (sin radian-t))
           y (* radius  (sin radian-s) (sin radian-t))
           z (* radius (cos radian-t))]
       [x y z]))))


(def texts
  (repeatedly
   300
   (fn []
     (let [r (* 300 (- (Math/log (rand))))
           th (* (rand) Math/PI)
           ph (* 2 (rand) Math/PI)]
       {:type :text
        :txt (generic-name)
        :pos [(* r (Math/cos th) (Math/sin ph))
              (* r (Math/sin th) (Math/sin ph))
              (* r (Math/cos th))]}))))


(defn content []
  (concat
   (for [r [20 50 400 800 1600]]
     {:type :line, :points (lissajeux-line r)})
   texts))
