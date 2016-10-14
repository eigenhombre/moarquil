(ns moarquil.render
  "
  Functionality for rendering and UI.  Purely math/geometrical
  functions live in `geom.clj`.
  "
  (:require [moarquil.geom :refer [content reset-content!]]
            [moarquil.util :refer [with-style with-shape with-matrix]]
            [quil.core :refer :all]))


(defn setup []
  (fill 0)
  (stroke 00))


(def ^:private dragging (atom false))
(def ^:private paused (atom false))
(defn toggle-paused [] (swap! paused not))


(def ^:private camera-positions (atom {:points-to [0 0 0]
                                       :theta 0
                                       :phi 0
                                       :r 1000}))


(def ^:private velocity (atom [0.00002
                               0.0001]))


(defn change-velocities-fractionally [f]
  (let [[vth vph] @velocity]
    (reset! velocity [(* vth f) (* vph f)])))


(defn update-camera-positions []
  (when-not (or @paused @dragging)
    (swap! camera-positions (fn [m]
                              (let [[vth vph] @velocity]
                                (-> m
                                    (update :theta + vth)
                                    (update :phi + vph)))))))


(defn update-camera-positions-continuously []
  (while true
    (update-camera-positions)
    (Thread/sleep 1)))


(defn ^:private draw-planet
  "
  Draw individual planet as a sphere in space.  Draw craters but only
  if display is not updating, since drawing them is slow.
  "
  [{:keys [r pos craters]}]
  (with-matrix
    (with-style
      (fill 255)
      (no-stroke)
      (apply translate pos)
      (sphere-detail 30)
      (sphere r)))

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
                                :planets true
                                :rings true}))


(defmacro deftoggle [name]
  (let [fn-name (->> name (str "toggle-") symbol)
        kw (keyword name)]
    `(defn ~fn-name [] (swap! to-render update ~kw not))))


(deftoggle spirals)
(deftoggle text)
(deftoggle spheres)
(deftoggle planets)
(deftoggle rings)


(defn render [objects]
  (doseq [{type_ :type :as l} objects]
    (cond
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
  (try
    (condp = (raw-key)
      \r (toggle-rings)
      \s (toggle-spheres)
      \p (toggle-planets)
      \t (toggle-text)
      \q (toggle-spirals)
      \R (reset-content!)
      \+ (change-velocities-fractionally 1.1)
      \- (change-velocities-fractionally 0.9)
      \space (toggle-paused)
      (println "Unknown key"))
    (catch Throwable t
      (prn t))))


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
