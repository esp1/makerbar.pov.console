(ns makerbar.pov.console.draw
  (:import [processing.core PImage])
  (:require [hiphip.int :as int]
            [makerbar.pov.console.state :as s]
            [makerbar.pov.console.util :as u]
            [quil.core :as q]))


(def graphics (atom nil))
(def img-graphics (atom nil))

(defn init []
  (reset! graphics (q/create-graphics s/pov-width s/pov-height))
  (reset! img-graphics (q/create-graphics s/pov-width s/pov-height)))


(defn adjust-color-channel
  "Apply brightness (+) and contrast (*) to color channel value"
  [^long value
   ^long brightness
   ^long contrast]
  (min (max 0 (+ (* value contrast) brightness)) 255))

(defn adjust-color
  "Apply brightness (+) and contrast (*) to rgb color"
  [c brightness contrast]
  (let [^long r (adjust-color-channel (bit-and (bit-shift-right c 16) 0xff) brightness contrast)
        ^long g (adjust-color-channel (bit-and (bit-shift-right c 8) 0xff) brightness contrast)
        ^long b (adjust-color-channel (bit-and c 0xff) brightness contrast)]
    (bit-or 0xff000000 (bit-shift-left r 16) (bit-shift-left g 8) b)))

(defn draw-image []
  (let [state @s/state
        graphics @graphics
        img-graphics @img-graphics]
    (u/draw graphics
            (.background graphics 0)
            
            (when-let [img (:image state)]
              (let [img-width (.width img)
                    img-height (.height img)
                    pimg (PImage. img-width img-height)
                    {:keys [contrast brightness
                            img-x-offset img-y-offset img-scale
                            pov-x-offset pov-y-offset]} state]
                ; adjust contrast and brightness
                (.loadPixels img)
                (let [img-pixels (.pixels img)
                      pimg-pixels (.pixels pimg)]
                  (doseq [i (range 0 (* img-width img-height))]
                    (let [color (int/aget img-pixels i)
                          adjusted-color (adjust-color color brightness contrast)]
                      (int/aset pimg-pixels i adjusted-color)))
                  (.updatePixels pimg))
                
                ; flip image
                (when (:flip-image state)
                  (.scale graphics -1 1)
                  (.translate graphics (- (- s/pov-width 1)) 0))
                
                ; draw image
                (let [x-graphics (u/draw img-graphics
                                         (doto img-graphics
                                           (.background 0)
                                           (.translate img-x-offset img-y-offset)
                                           (.scale img-scale)
                                           (.image pimg 0 0)))]
                  (doseq [x (range (- pov-x-offset s/pov-width) s/pov-width s/pov-width)
                          y (range (- pov-y-offset s/pov-height) s/pov-height s/pov-height)]
                    (.image graphics x-graphics x y))))))
    
    (q/image graphics 0 0)))
