(ns moarquil.util
  (:require [quil.core :refer :all]))


(defmacro defcontext
  "
  Create a contextual macro with setup and teardown.

  Example:

  (defcontext foo (setup-expr) (teardown-expr))

  expands to

  (defmacro with-foo [& body]
    `(do
       (setup-expr)
       (try
         ~@body
         (finally
           (teardown-expr)))))

  Brain salve: http://hubpages.com/technology/Clojure-macro-writing-macros
  "
  [nom setup teardown]
  `(defmacro ~(symbol (str "with-" nom))
     [~'& body#]
     `(do
        ~'~setup
        (try
          ~@body#
          (finally
            ~'~teardown)))))


(defcontext style (push-style) (pop-style))
(defcontext shape (begin-shape) (end-shape))
(defcontext matrix (push-matrix) (pop-matrix))
