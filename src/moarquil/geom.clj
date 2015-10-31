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


(defn texts* []
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


(def texts (atom (texts*)))
(defn reset-texts! [] (reset! texts (texts*)))


(defn spheres* []
  (let [max-pos 800
        positions (->> (repeatedly 50 (partial rand-int max-pos))
                       (map (partial + (/ max-pos (- 2))))
                       (partition 3))]
    (for [pos positions]
      {:type :sphere
       :radius (rand-int 30)
       :value (rand-int 255)
       :origin pos})))


(def spheres (atom (spheres*)))
(defn reset-spheres! [] (reset! spheres (spheres*)))


(defn content []
  (concat
   @spheres
   (for [r [20 50 400 800 1600]]
     {:type :line, :points (lissajeux-line r)})
   [{:type :ring, :pos [0 0 0], :r1 200, :r2 350, :dr 3, :rotx 0, :color 180}
    {:type :ring, :pos [0 0 0], :r1 700, :r2 900, :dr 10, :rotx 45, :color 50}]
   @texts))


(defn reset-content! []
  (reset-spheres!)
  (reset-texts!))
