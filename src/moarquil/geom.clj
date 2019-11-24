(ns moarquil.geom
  "
  This is the namespace for math-y things and for defining the objects
  in the \"universe\", without worrying about how they are rendered.
  "
  (:require [clojure.core.matrix :as m]
            [namejen.names :as nom]))


(set! *warn-on-reflection* true)

;; <em>Text Names</em>

(defn ^:private texts*
  "
  Generate a mysterious cloud of names distributed randomly throughout
  a spherical volume, most of them close to origin (exponential
  falloff).

  Are they names of ships?  Of lost artifacts adrift in space?
  Asteroids?  Only you can decide.
  "
  []
  (repeatedly
   100
   (fn []
     (let [r (* 300 (- (Math/log (rand))))
           th (* (rand) Math/PI)
           ph (* 2 (rand) Math/PI)]
       {:type :text
        :txt (nom/generic-name)
        :pos [(* r (Math/cos th) (Math/sin ph))
              (* r (Math/sin th) (Math/sin ph))
              (* r (Math/cos th))]}))))


;; Store state of names and allow it to be resettable.
(def ^:private texts (atom (texts*)))
(defn reset-texts! [] (reset! texts (texts*)))

;; <em>Spheres (small planets)</em>

(defn ^:private spheres*
  "
  Generate spheres of different shades and sizes, distributed
  throughout a large, cubic space.
  "
  []
  (let [max-pos 800
        positions (->> (partial rand-int max-pos)
                       (repeatedly 50)
                       (map (partial + (/ max-pos (- 2))))
                       (partition 3))]
    (for [pos positions]
      {:type :sphere
       :radius (rand-int 30)
       :value (rand-int 255)
       :origin pos})))


;; Store state of spheres and allow it to be resettable
(def ^:private spheres (atom (spheres*)))
(defn reset-spheres! [] (reset! spheres (spheres*)))


;; <em>(Large) Planets</em>

(defn ^:private crater-points
  "
  Generate \"craters\" on a \"planet\", by drawing circles just
  outside the surface of the sphere.

  This is basically just a bunch of linear algebra to do the
  appropriate translations and rotations.
  "
  [planet-radius
   planet-pos
   crater-radius]
  (let [[theta phi] [(rand Math/PI) (rand (* 2 Math/PI))] ;; crater angles
        [x y z] planet-pos
        ;; Translate by postition of world:
        tm' (m/matrix [[1 0 0 x]
                       [0 1 0 y]
                       [0 0 1 z]
                       [0 0 0 1]])
        ;; Rotate around z by phi:
        rz' (m/matrix [[(Math/cos phi) (- (Math/sin phi)) 0 0]
                       [(Math/sin phi) (Math/cos phi)     0 0]
                       [0         0             1 0]
                       [0         0             0 1]])
        ;; Rotate around y by theta:
        ry' (m/matrix [[(Math/cos theta)     0 (Math/sin theta) 0]
                       [0               1 0           0]
                       [(- (Math/sin theta)) 0 (Math/cos theta) 0]
                       [0               0 0           1]])
        txform (m/mmul tm'
                       rz'
                       ry')]
    (for [_ (range 100)]
      (let [eta (rand (* 2 Math/PI))
            v (m/matrix [(* crater-radius (Math/cos eta))
                         (* crater-radius (Math/sin eta))
                         planet-radius
                         1])]
        (butlast (m/mmul txform v))))))


(defn ^:private gen-planet
  "
  Generate planet object, including surface craters provided by
  `crater-points`.
  "
  []
  (let [max-pos 1200
        radius (+ 100 (rand-int 100))
        pos [(- (rand-int max-pos) (/ max-pos 2))
             (- (rand-int max-pos) (/ max-pos 2))
             (- (rand-int max-pos) (/ max-pos 2))]
        min-craters 10
        max-craters 200
        craters (for [_ (range min-craters max-craters)]
                  (crater-points radius pos (rand 30)))]
    {:type :planet, :r radius, :pos pos, :craters craters}))


(defn ^:private planets*
  "Generate 4 to 8 planets."
  []
  (repeatedly (+ 4 (rand-int 5)) gen-planet))


;; Save planet state and allow it to be resettable.
(def ^:private planets (atom (planets*)))
(defn reset-planets! [] (reset! planets (planets*)))


;; <em>Rings (sort of like asteroid belts)</em>

(defn ^:private gen-ring
  "
  Generate a bunch of \"rock\" positions spread throughout a ring
  shape (random positions spread out in between two radii).
  "
  [pos r1 r2 dr rotx color]
  (let [points
        (for [_ (range 10000)]
          (let [r (+ r1 (rand (- r2 r1)))
                theta (rand (* 2 Math/PI))]
            [(* r (Math/cos theta)), (* r (Math/sin theta)), 0]))]
    {:type :ring
     :pos pos
     :r1 r1
     :r2 r2
     :dr dr
     :rotx rotx
     :color color
     :points points}))


;; Just two rings, for now.
(defn ^:private rings* []
  [(gen-ring [0 0 0] 200 350 3 0 180)
   (gen-ring [0 0 0] 700 900 10 (rand-int 90) 50)])


;; State and reset logic for rings
(def ^:private rings (atom (rings*)))
(defn reset-rings! [] (reset! rings (rings*)))


;; <em>Cosmic Spirals</em>

;; FIXME: from quil, make better:
(defn line-join-points [interleaved-points]
  (lazy-seq
   (let [head (take 2 interleaved-points)]
     (if (= 2 (count head))
       (cons (apply concat head) (line-join-points (drop 1 interleaved-points)))))))

(defn radians [deg] (* deg (/ Math/PI 180)))

(defn ^:private gen-spiral
  "
  Generate spiral as a series of line segments with fixed radius on a
  sphere but gradually changing zenith angle.
  "
  [radius]
  (line-join-points
   (for [t (map (partial * 0.05) (range 0 3610))]
     (let [s (* t 100)
           radian-s (radians s)
           radian-t (radians t)
           x (* radius  (Math/cos radian-s) (Math/sin radian-t))
           y (* radius  (Math/sin radian-s) (Math/sin radian-t))
           z (* radius (Math/cos radian-t))]
       [x y z]))))

;; Do five spirals of different radii.  Not resettable (since there is
;; no randomness).
(def ^:private spirals
  (for [r [40
           100
           400
           800
           1600]]
    {:type :spiral
     :points (gen-spiral r)}))

(defn ^:private gooey-boxes [n]
  (loop [boxes [{:type :box
                 :coords [0 0 0]}]
         n n]
    (if (zero? n)
      (distinct boxes)
      (let [{:keys [coords]} (last boxes)
            rand-index (rand-int 3)
            new-coords (update coords
                               rand-index
                               (partial + (rand-nth [-10 10])))]
        (recur (conj boxes {:type :box
                            :coords new-coords})
               (dec n))))))

(defn ^:private boxes* []
  (let [box-region-size 1000
        coord-fn (fn [] (- (/ box-region-size 2)
                           (rand-int box-region-size)))]
    (repeatedly 1000
                (fn [] {:type :box
                        :coords [(coord-fn)
                                 (coord-fn)
                                 (coord-fn)]}))))

(def ^:private boxes (atom (gooey-boxes 3000)))
;;(def ^:private boxes (atom (boxes*)))
;; <em>Public functions, for rendering and resetting everything.</em>

(defn content
  "
  Fetch everything to render, from current state.
  "
  []
  (concat
   @spheres
   @rings
   @planets
   @texts
   spirals
   @boxes))


(defn reset-content!
  "
  Reset everything that is resettable so the viewer sees a new
  \"universe.\"
  "
  []
  (reset-spheres!)
  (reset-planets!)
  (reset-texts!))
