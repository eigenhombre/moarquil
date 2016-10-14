(ns moarquil.core
  "
  Main namespace.

  More about this project at `https://github.com/eigenhombre/moarquil`.
  "
  (:gen-class)
  (:require [moarquil.render :refer :all]
            [quil.core :refer :all]))





;; Save our app in an atom so we can reset it (only used during
;; REPL-driven development)
(defonce app (atom nil))


(defn -main
  "
  Main function, called when running `java -jar target/moarquil.jar`
  "
  []
  (let [thisapp (quil.applet/applet
                 :size [1600 1200]
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

;; Stuff inside `comment` expressions is not executed, unless you
;; explicitly evaluate it in your editor.

;; Clear existing display (for REPLing):
;; http://stackoverflow.com/questions/12545570/\
;; how-to-destroy-processing-papplet-without-calling-exit
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
    (run-marginalia ["src"])
    (clojure.java.shell/sh "open" "docs/uberdoc.html")))
