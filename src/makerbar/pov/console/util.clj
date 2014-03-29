(ns makerbar.pov.console.util
  (:require [quil.core :as q]))

(defmacro with-matrix
  [& body]
  `(do
     (q/push-matrix)
     ~@body
     (q/pop-matrix)))

(defmacro with-style
  [& body]
  `(do
     (q/push-style)
     ~@body
     (q/pop-style)))

(defmacro draw
  [g & body]
  `(let [g# ~g]
     (.beginDraw g#)
     ~@body
     (.endDraw g#)
     g#))
