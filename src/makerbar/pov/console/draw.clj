(ns makerbar.pov.console.draw
  (:import [processing.core PImage])
  (:require [hiphip.int :as int]
            [makerbar.pov.console.images :as i]
            [makerbar.pov.console.state :as s]
            [makerbar.pov.console.util :as u]
            [quil.core :as q]))


(def pov-graphics (atom nil))
(def tile-graphics (atom nil))

(defn init []
  (reset! pov-graphics (q/create-graphics s/pov-width s/pov-height))
  (reset! tile-graphics (q/create-graphics s/pov-width s/pov-height)))


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
  (when-let [img (:image @s/state)]
    ; draw scaled image to pov-graphics
    (u/draw @pov-graphics
            (let [{:keys [offset scale]} (i/scale-image-instructions img s/pov-width s/pov-height)
                  [offset-x offset-y] offset]
              (doto @pov-graphics
                (.background 0)
                (.scale scale)
                (.image img offset-x offset-y)))
            
            ; flip image
            (when (:flip-image @s/state)
              (doto @pov-graphics
                (.scale -1 1)
                (.translate (- (- s/pov-width 1)) 0))))
    
    ; adjust contrast and brightness
    (let [{:keys [contrast brightness]} @s/state]
      (.loadPixels @pov-graphics)
      (let [pixels (.pixels @pov-graphics)]
        (doseq [i (range 0 (* s/pov-width s/pov-height))]
          (let [color (int/aget pixels i)
                adjusted-color (adjust-color color brightness contrast)]
            (int/aset pixels i adjusted-color)))
        (.updatePixels @pov-graphics)))
    
    ; draw tile to tile-graphics
    (let [{[img-x-offset img-y-offset] :img-offset
           img-scale :img-scale} @s/state]
      (u/draw @tile-graphics
              (doto @tile-graphics
                (.background 0)
                (.scale img-scale)
                (.image @pov-graphics img-x-offset img-y-offset))))
            
    ; tile images (overwrite pov-graphics)
    (let [{[pov-x-offset pov-y-offset] :pov-offset} @s/state]
      (u/draw @pov-graphics
              (doseq [x (range (- pov-x-offset s/pov-width) s/pov-width s/pov-width)
                      y (range (- pov-y-offset s/pov-height) s/pov-height s/pov-height)]
                (.image @pov-graphics @tile-graphics x y))))
  
    (q/image @pov-graphics 0 0)))
