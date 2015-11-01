(ns moarquil.geom
  (:require [clojure.core.matrix :as m]
            [namejen.names :refer [generic-name]]
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
   100
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


(defn crater-points [planet-radius
                     planet-pos
                     crater-radius]
  (let [[theta phi] [(rand PI) (rand (* 2 PI))] ;; crater angles
        [x y z] planet-pos
        ;; Translate by postition of world:
        tm' (m/matrix [[1 0 0 x]
                       [0 1 0 y]
                       [0 0 1 z]
                       [0 0 0 1]])
        ;; Rotate around z by phi:
        rz' (m/matrix [[(cos phi) (- (sin phi)) 0 0]
                       [(sin phi) (cos phi)     0 0]
                       [0         0             1 0]
                       [0         0             0 1]])
        ;; Rotate around y by theta:
        ry' (m/matrix [[(cos theta)     0 (sin theta) 0]
                       [0               1 0           0]
                       [(- (sin theta)) 0 (cos theta) 0]
                       [0               0 0           1]])
        txform (m/mmul tm'
                       rz'
                       ry')]
    (for [_ (range 30)]
      (let [eta (rand (* 2 PI))
            v (m/matrix [(* crater-radius (cos eta))
                         (* crater-radius (sin eta))
                         planet-radius
                         1])]
        (butlast (m/mmul txform v))))))


(defn gen-planet []
  (let [max-pos 1200
        radius (+ 100 (rand-int 100))
        pos [(- (rand-int max-pos) (/ max-pos 2))
             (- (rand-int max-pos) (/ max-pos 2))
             (- (rand-int max-pos) (/ max-pos 2))]
        craters (for [_ (range (Math/pow (rand-int 3) 10))]
                  (crater-points radius pos (rand 30)))]
    {:type :planet, :r radius, :pos pos, :craters craters}))


(defn planets* []
  (repeatedly (+ 4 (rand-int 5)) gen-planet))


(def planets (atom (planets*)))
(defn reset-planets! [] (reset! planets (planets*)))


(defn rings* []
  [{:type :ring, :pos [0 0 0], :r1 200, :r2 350, :dr 3, :rotx 0, :color 180}
   {:type :ring
    :pos [0 0 0]
    :r1 700
    :r2 900
    :dr 10
    :rotx (rand-int 90)
    :color 50}])


(def rings (atom (rings*)))
(defn reset-rings! [] (reset! rings (rings*)))


(defn content []
  (concat
   @spheres
   (for [r [400
            800
            1600
            ]]
     {:type :line
      :points (lissajeux-line r)})
   @rings
   @planets
   @texts))


(defn reset-content! []
  (reset-spheres!)
  (reset-planets!)
  (reset-texts!))
