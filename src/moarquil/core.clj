;; This animation project evolved over time as a way to learn Quil
;; better and to make a sort of immersive environment, reminiscent of
;; a "solar system" that one might explore in a space simulation game.
;; Also as an excuse to play with my random name generator
;; (`https://github.com/eigenhombre/namejen`).

;; Some screenshots taken while developing the program can be found at
;; `http://parentheticalworlds.tumblr.com`.

;; To run the program, first run `lein überjar`, then:

;; `java -jar target/moarquil.jar`.

;; More about this project at `https://github.com/eigenhombre/moarquil`.

;; <script src='https://cdn.mathjax.org/mathjax/latest/MathJax.js?config=TeX-AMS-MML_HTMLorMML'></script>

;; <hr>

(ns moarquil.core
  "
  Main namespace.
  "
  (:gen-class)
  (:require [moarquil.render :refer :all]
            [quil.core :refer :all]))

;; Save our app in an atom so we can reset it (only used during
;; REPL-driven development)
(defonce app (atom nil))


(defn -main
  "
  Main function; just set up the Quil app and run it in a separate
  thread.  Also move window to top left corner.
  "
  []
  (let [thisapp (quil.applet/applet
                 :size [1600 1200]
                 :settings #(smooth 2)
                 :setup setup
                 :draw draw
                 :key-typed key-press
                 :mouse-dragged mouse-dragged
                 :mouse-pressed mouse-pressed
                 :mouse-released mouse-released
                 :mouse-wheel mouse-wheel
                 :renderer :opengl)]
    (.setLocation (.frame thisapp) 0 0)
    (future (update-camera-positions-continuously))
    (reset! app thisapp)))


;; <em>Tools for Developing at the REPL.</em>

;; The remainder of the namespace is stuff I put in for convenience
;; during development. Stuff inside `comment` expressions is not
;; executed, unless you explicitly evaluate it in your editor.

;; Clear existing display (for REPLing). See
;; `http://stackoverflow.com/questions/12545570/how-to-destroy-processing-papplet-without-calling-exit`
(comment
  (do
    (when @app
      (.setVisible (.frame @app) false)
      (reset! app nil))

    (-main)))


;; Re-generate Marginalia documentation at will; normally one runs
;; `lein marg`, but doing it at the REPL is much faster.
(comment
  (do
    (require '[marginalia.core :refer [run-marginalia]])
    (run-marginalia ["src/moarquil/core.clj"
                     "src/moarquil/render.clj"
                     "src/moarquil/geom.clj"
                     "src/moarquil/util.clj"])
    (clojure.java.shell/sh "open" "docs/uberdoc.html")))
