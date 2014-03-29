(ns makerbar.pov.console.draw
  (:import [processing.core PImage])
  (:require [hiphip.int :as int]
            [makerbar.pov.console.state :as s]
            [makerbar.pov.console.util :refer (draw)]
            [quil.core :as q]))


(defn bounded [x] (min (max 0 x) 255))

(defn adjust-color-channel
  "Apply brightness (+) and contrast (*) to color channel value"
  [value brightness contrast]
  (bounded (int (+ (* value contrast) brightness))))

(defn adjust-color
  "Apply brightness (+) and contrast (*) to rgb color"
  [c brightness contrast]
  (let [r (adjust-color-channel (bit-and (bit-shift-right c 16) 0xff) brightness contrast)
        g (adjust-color-channel (bit-and (bit-shift-right c 8) 0xff) brightness contrast)
        b (adjust-color-channel (bit-and c 0xff) brightness contrast)]
    (bit-or 0xff000000 (bit-shift-left r 16) (bit-shift-left g 8) b)))

(defn draw-image []
  (draw @s/graphics
        (.background @s/graphics 0)
        
        (when-let [img (:image @s/state)]
          (let [img-width (.width img)
                img-height (.height img)
                pimg (PImage. img-width img-height)
                {:keys [contrast brightness
                        img-x-offset img-y-offset img-scale
                        pov-x-offset pov-y-offset]} @s/state]
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
            (when (:flip-image @s/state)
              (.scale @s/graphics -1 1)
              (.translate @s/graphics (- (- s/pov-width 1)) 0))
            
            ; draw image
            (let [x-graphics (draw @s/img-graphics
                                   (doto @s/img-graphics
                                     (.background 0)
                                     (.translate img-x-offset img-y-offset)
                                     (.scale img-scale)
                                     (.image pimg 0 0)))]
              (doseq [x (range (- pov-x-offset s/pov-width) s/pov-width s/pov-width)
                      y (range (- pov-y-offset s/pov-height) s/pov-height s/pov-height)]
                (.image @s/graphics x-graphics x y))))))
  
  (q/image @s/graphics 0 0))

(defn display-image-list [])
(defn display-instructions [])
