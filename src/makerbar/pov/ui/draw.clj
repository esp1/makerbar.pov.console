(ns makerbar.pov.ui.draw
  (:require [hiphip.int :as int]
            [makerbar.pov.rendersphere :as rendersphere]
            [makerbar.pov.state :as s]
            [makerbar.pov.ui.images :as img]
            [makerbar.pov.ui.processing :as p]))


(def pov-graphics (atom nil))
(def tile-graphics (atom nil))

(defn init []
  (reset! pov-graphics (p/create-graphics s/pov-width s/pov-height))
  (reset! tile-graphics (p/create-graphics s/pov-width s/pov-height)))


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

(defn pov-render
  "Renders draw-fn to the POV graphics context. Applies POV display effects such as contrast/brightness adjustments."
  [draw-fn]
  (p/with-graphics @pov-graphics
                   (p/draw
                     (draw-fn)

                     ; flip image
                     (when (s/get-state :flip-image)
                       (p/scale -1 1)
                       (p/translate (- (- s/pov-width 1)) 0))))

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
  (let [{img-offset :img-offset
         img-scale  :img-scale} @s/state]
    (p/with-graphics @tile-graphics
                     (p/draw
                       (p/background 0)
                       (p/scale img-scale)
                       (p/image @pov-graphics img-offset))))

  ; tile images (overwrite pov-graphics)
  (let [{[pov-x-offset pov-y-offset] :pov-offset} @s/state]
    (p/with-graphics @pov-graphics
                     (p/draw
                       (doseq [x (range (- pov-x-offset s/pov-width) s/pov-width s/pov-width)
                               y (range (- pov-y-offset s/pov-height) s/pov-height s/pov-height)]
                         (p/image @tile-graphics x y)))))

  (p/image @pov-graphics 0 0))

(defn pov-view
  "Shows a view of the POV display. Takes a draw-fn which will be used to render the POV display contents."
  [draw-fn]
  (p/with-matrix
    (let [{:keys [offset scale]} (img/scale-image-instructions s/pov-width s/pov-height (* 0.9 (p/width)) (* 0.9 (p/height)))]
      (p/translate (* 0.05 (p/width)) (* 0.05 (p/height)))
      (p/translate offset)
      (p/scale scale))

    (pov-render draw-fn)

    ; send image data to POV display
    (if-let [data (.pixels @pov-graphics)]
      (rendersphere/send-data data))

    ; draw frame
    (p/with-style
      (p/stroke 200)
      (p/no-fill)
      (p/rect -1 -1 (+ s/pov-width 1) (+ s/pov-height 1)))))