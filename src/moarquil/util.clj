(ns moarquil.util
  "
  Utilities namespace.
  "
  (:require [quil.core :refer :all]))


(defmacro defcontext
  "
  Create a contextual macro[1] with setup and teardown.  The teardown
  executes even if the body raises an exception.

  [1] Macro-writing-macros hurt my brain.  This helps:

  `http://hubpages.com/technology/Clojure-macro-writing-macros`.

  See also:

  `http://eigenhombre.com/macro-writing-macros.html`
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


;; The actual context macros, with specified setup and teardown steps.
;; Example usage: `(with-style ...body... )`.
(defcontext style (push-style) (pop-style))
(defcontext shape (begin-shape) (end-shape))
(defcontext matrix (push-matrix) (pop-matrix))
