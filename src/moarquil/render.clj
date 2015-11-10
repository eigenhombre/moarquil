(ns moarquil.render
  (:require [moarquil.geom :refer [content reset-content!]]
            [moarquil.util :refer :all]
            [quil.core :refer :all]))


(defn setup []
  (fill 0)
  (smooth 8)
  (stroke 00))


(def ^:private dragging (atom false))
(def ^:private paused (atom false))
(defn toggle-paused [] (swap! paused not))


(def ^:private camera-positions (atom {:points-to [0 0 0]
                                       :theta 0
                                       :phi 0
                                       :r 1000}))


(defn update-camera-positions []
  (when-not (or @paused @dragging)
      (swap! camera-positions (fn [m]
                                (-> m
                                    (update :theta + 0.0000002)
                                    (update :phi + 0.000001))))))


(defn update-camera-positions-continuously []
  (while true
    (update-camera-positions)
    (Thread/sleep 1)))


(defn ^:private draw-planet [{:keys [r pos craters]}]
  (with-matrix
    (with-style
      (fill 255)
      (no-stroke)
      (apply translate pos)
      (sphere-detail 30)
      (sphere r)))

  ;; Draw craters
  (when (and @paused (not @dragging))
    (with-style
      (stroke 80)
      (doseq [c craters]
        (doseq [p c]
          (apply point p))))))


(defn ^:private draw-sphere [{:keys [value origin radius]}]
  (with-matrix
    (with-style
      (fill value)
      (no-stroke)
      (sphere-detail 15)
      (translate origin)
      (sphere radius))))


(defn ^:private draw-ring [{:keys [pos r1 r2 dr rotx color points]}]
  (with-matrix
    (apply translate pos)
    (rotate-x rotx)
    (with-style
      (doseq [p points]
        (apply point p)))))


(defn ^:private draw-spiral [l]
  (with-style
    (stroke 150)
    (doseq [p (:points l)]
      (stroke-weight (min 0.1 (- (/ (last p) 300))))
      (apply line p))))


(defn ^:private draw-text [l]
  (apply (partial text (:txt l))
             (:pos l)))


(def ^:private to-render (atom {:spirals true
                                :text true
                                :spheres true
                                :cylinders true
                                :planets true
                                :rings true}))


(defmacro deftoggle [name]
  (let [fn-name (->> name (str "toggle-") symbol)
        kw (keyword name)]
    `(defn ~fn-name [] (swap! to-render update ~kw not))))


(deftoggle spirals)
(deftoggle cylinders)
(deftoggle text)
(deftoggle spheres)
(deftoggle planets)
(deftoggle rings)


(defn draw-cylinder [{:keys [pos r h rotx roty]}]
  (with-matrix
    (translate pos)
    (rotate-x rotx)
    (rotate-y roty)
    (with-style
      (fill 200 200 200 100)
      (doseq [[phi1 phi2] (partition 2 1 (range 0 370 10))]
        (let [phi1 (-> phi1 (* 2 PI) (/ 360))
              phi2 (-> phi2 (* 2 PI) (/ 360))
              x1 (* r (cos phi1))
              y1 (* r (sin phi1))
              x2 (* r (cos phi2))
              y2 (* r (sin phi2))]
          (stroke 180 180 180 50)
          (with-shape
            (vertex x1 y1 0)
            (vertex x2 y2 0)
            (vertex x2 y2 h)
            (vertex x1 y1 h)
            (vertex x1 y1 0))
          (stroke 0)
          (line x1 y1 0 x2 y2 0)
          (line x1 y1 h x2 y2 h))))))


(defn render [objects]
  (doseq [{type_ :type :as l} objects]
    (cond
      (and (= type_ :cylinder)
           (:cylinders @to-render))
      (draw-cylinder l)

      (and (= type_ :spiral)
           (:spirals @to-render))
      (draw-spiral l)

      (and (= type_ :text)
           (:text @to-render))
      (draw-text l)

      (and (= type_ :sphere)
           (:spheres @to-render))
      (draw-sphere l)

      (and (= type_ :ring)
           (:rings @to-render))
      (draw-ring l)

      (and (= type_ :planet)
           (:planets @to-render))
      (draw-planet l))))


(defn key-press []
  (condp = (raw-key)
    \r (toggle-rings)
    \s (toggle-spheres)
    \p (toggle-planets)
    \t (toggle-text)
    \q (toggle-spirals)
    \R (reset-content!)
    \space (toggle-paused)))


(defn draw []
  (background 220)
  (let [theta (:theta @camera-positions)
        phi (:phi @camera-positions)
        r (:r @camera-positions)]
    (camera (* r (Math/cos phi) (Math/sin theta))
            (* r (Math/sin phi) (Math/sin theta))
            (* r (Math/cos theta))
            0 0 0
            0 1 1))
  (render (content)))


(defn mouse-dragged []
  (reset! dragging true)
  (let [delx (- (mouse-x)
                (pmouse-x))
        mdx (/ delx 3)
        dely (- (pmouse-y)
                (mouse-y))
        mdy (/ dely 3)]
    (swap! camera-positions update :phi - (radians mdx))
    (swap! camera-positions update :theta + (radians mdy))))


(defn mouse-pressed [] (reset! dragging true))
(defn mouse-released [] (reset! dragging false))
(defn mouse-wheel [amount]
  (reset! dragging true)
  (future (Thread/sleep 1000)
          (reset! dragging false))
  (swap! camera-positions update :r #(max 1 (+ % (* 3 amount)))))
