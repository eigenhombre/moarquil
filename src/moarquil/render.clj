(ns moarquil.render
  (:require [moarquil.geom :refer [content reset-content!]]
            [quil.core :refer :all]))


(defn setup []
  (fill 0)
  (smooth)
  (stroke 00))


(def paused (atom false))
(defn toggle-paused [] (swap! paused not))


(def frame-counter (atom 0))
(defn update-camera-positions []
  (while true
    (if-not @paused (swap! frame-counter inc))
    (Thread/sleep 1)))


(defn- draw-planet [{:keys [r pos craters]}]
  (push-matrix)
  (push-style)
  (fill 255)
  (no-stroke)
  (apply translate pos)
  (sphere-detail 30)
  (sphere r)
  (pop-style)
  (pop-matrix)

  ;; Draw craters
  (when @paused
    (push-style)
    (stroke 80)
    (doseq [c craters]
      (doseq [p c]
        (apply point p)))
    (pop-style)))


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


(defn- draw-ring [{:keys [pos r1 r2 dr rotx color points]}]
  (push-matrix)
  (apply translate pos)
  (rotate-x rotx)
  (push-style)
  (doseq [p points]
    (apply point p))
  (pop-style)
  (pop-matrix))


(defn- draw-spiral [l]
  (when @paused
    (push-style)
    (stroke 150)
    (doseq [p (:points l)]
      (stroke-weight (min 0.1 (- (/ (last p) 300))))
      (apply line p))
    (pop-style)))


(defn- draw-text [l]
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
  (translate (/ (width) 2) (/ (height) 2) 0)
  (let [theta (* (+ 10000 @frame-counter) -0.000025)
        phi (* @frame-counter 0.000005)
        r (+ 1000 (* 500 (Math/cos (* 0.0001 @frame-counter))))]
    (camera (* r (Math/cos phi) (Math/sin theta))
            (* r (Math/sin phi) (Math/sin theta))
            (* r (Math/cos theta))
            0 0 0
            0 1 1))
  (render (content)))
