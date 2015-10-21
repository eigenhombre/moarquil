(ns moarquil.core
  (:require [quil.core :as q]
            [quil.middleware :as m]))


(comment
  (-> '(defsketch foo)
      macroexpand-1
      macroexpand-1)
  ;;=>

  
(defn setup []
  (q/smooth)                          ;; Turn on anti-aliasing
  (q/frame-rate 100)                    ;; Set framerate to 1 FPS
  (q/background 200))                 ;; Set the background colour to
;; a nice shade of grey.

  (defn draw []
 (q/stroke (q/random 255))             ;; Set the stroke colour to a random grey
  (q/stroke-weight (q/random 10))       ;; Set the stroke thickness randomly
  (q/fill (q/random 255))               ;; Set the fill colour to a random grey

  (let [diam (q/random 100)             ;; Set the diameter to a value between 0 and 100
        x    (q/random (q/width))       ;; Set the x coord randomly within the sketch
        y    (q/random (q/height))]     ;; Set the y coord randomly within the sketch
    (q/ellipse x y diam diam)))
  (def foo (quil.applet/applet
            :size [400 400]
            :setup setup
            :draw draw))
  
  (-> foo .exit)













  )


