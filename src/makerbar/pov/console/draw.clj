(ns makerbar.pov.console.draw
  (:import [processing.core PImage])
  (:require [makerbar.pov.console.state :as s]
            [makerbar.pov.console.util :refer (draw)]
            [quil.core :as q]))


; Drawing fns

(defn adjust-color [])

(defn adjust-color
  "Apply contrast (*) and brightness (+) to color"
  [c contrast brightness]
  (letfn [(bounded [x] (min (max 0 x) 255))
          (adjust [x] (bounded (int (+ (* x contrast) brightness))))]
    (let [r (adjust (bit-and (bit-shift-right c 16) 0xff))
          g (adjust (bit-and (bit-shift-right c 8) 0xff))
          b (adjust (bit-and c 0xff))]
      (bit-or 0xff000000 (bit-shift-left r 16) (bit-shift-left g 8) b))))

(defn draw-image []
  (draw @s/graphics
        (.background @s/graphics 0)
        
        (when-let [img (:image @s/state)]
          (println "got" img)
          (let [img-width (.width img)
                img-height (.height img)
                pimg (PImage. img-width img-height)
                {:keys [contrast brightness
                        img-x-offset img-y-offset img-scale
                        pov-x-offset pov-y-offset]} @s/state]
            ; adjust contrast and brightness
            (println "load pixels")
            (.loadPixels img)
            (println "adjusting contrast and brightness")
            (let [img-pixels (.pixels img)
                  pimg-pixels (.pixels pimg)]
              (println "range:" (* img-width img-height))
              (doseq [i (range 0 (* img-width img-height))]
                (let [color (aget img-pixels i)
                      adjusted-color (adjust-color color contrast brightness)]
                  (aset pimg-pixels i adjusted-color)))
              (println "end range")
              (.updatePixels pimg))
            
            ; flip image
            (when (:flip-image @s/state)
              (println "flip image")
              (.scale @s/graphics -1 1)
              (.translate @s/graphics (- (- s/pov-width 1)) 0))
            
            ; draw image
            (println "draw image")
            (let [x-graphics (draw @s/img-graphics
                                   (doto @s/img-graphics
                                     (.background 0)
                                     (.translate img-x-offset img-y-offset)
                                     (.scale img-scale)
                                     (.image pimg 0 0)))]
              (doseq [x (range (- pov-x-offset s/pov-width) s/pov-width)
                      y (range (- pov-y-offset s/pov-height) s/pov-height)]
                (.image @s/graphics x-graphics x y))))))
  
  (q/image @s/graphics 0 0)
  (println "done"))

(defn display-image-list [])
(defn display-instructions [])
