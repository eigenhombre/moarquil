(ns moarquil.core
  (:gen-class)
  (:require [moarquil.geom :refer [content]]
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
  (smooth 2)
  (stroke 00))


(defn- render [objects]
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
      (do
        (push-matrix)
        (push-style)
        (fill (:value l))
        (stroke 0)
        (sphere-detail 10)
        (translate (:origin l))
        (sphere (:radius l))
        (pop-style)
        (pop-matrix)))))


(defn draw []
  (background 250)
  (translate (/ (width) 2) (/ (height) 2) 0)
  (rotate-y (* (frame-count) 0.0005))
  (rotate-x (* (frame-count) -0.0025))
  (render (content)))


(defn -main []
  (let [thisapp (quil.applet/applet
                 :size [840 1200]
                 :setup setup
                 :draw draw
                 :renderer :opengl)]
    (.setLocation (.frame thisapp) 0 0)
    (reset! app thisapp)))
