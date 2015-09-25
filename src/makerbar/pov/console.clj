(ns makerbar.pov.console
  (:require [makerbar.pov.controller.keyboard :as k]
            [makerbar.pov.net :as n]
            [makerbar.pov.state :as s]
            [makerbar.pov.ui.draw :as d]
            [makerbar.pov.ui.images :as i]
            [makerbar.pov.ui.processing :as p]))

(def time-t (atom (System/currentTimeMillis)))

(defn draw []
  (when (s/get-state :console-mirror)
    (p/scale -1 1)
    (p/translate (- (p/width)) 0))

  ; clear
  (p/background 0)

  ; info
  (p/text (str "Display dimensions: " s/pov-width " x " s/pov-height) 40 (- 80 20 (p/text-descent)))

  (let [t @time-t
        now (System/currentTimeMillis)
        fps (float (/ 1000 (- now t)))]
    (reset! time-t now)

    ; display frames per second
    (p/with-style
      (p/stroke 255)
      (p/text (str "Processing FPS: " (format "%.1f" fps)) 40 40))

    ; rotate
    (s/inc-pov-offset [(* (s/get-state :rotation-speed) (s/get-state :rotation-direction)) 0]))

  (p/with-matrix
    ;    (p/translate 40 80)
    ;    (p/scale 3)

    (let [{:keys [offset scale]} (i/scale-image-instructions s/pov-width s/pov-height (* 0.9 (p/width)) (* 0.9 (p/height)))]
      (p/translate (* 0.05 (p/width)) (* 0.05 (p/height)))
      (p/translate offset)
      (p/scale scale))

    ; draw image
    (d/draw-image)

    ; send image data to POV display
    (if-let [pov-addr @s/pov-addr]
      (if-let [data (.pixels @d/pov-graphics)]
        (n/pov-send-data pov-addr data)))

    ; draw frame
    (p/with-style
      (p/stroke 200)
      (p/no-fill)
      (p/rect -1 -1 (+ s/pov-width 1) (+ s/pov-height 1))))

  ; image list
  (p/with-matrix
    (p/translate (- (p/width) 500) 100)

    #_(p/with-matrix
        (when-let [img (i/get-selected-image)]
          (let [{:keys [offset scale]} (i/scale-image-instructions (.width img) (.height img) s/pov-width s/pov-height)]
            (p/scale scale)
            (p/image img offset))))

    (p/with-style
      (p/stroke 255)
      (p/text (i/display-image-list) 0 120)))

  ; instructions
  (p/with-style
    (p/stroke 255)
    (p/text (k/display-keyboard-controls) 40 (- (p/height) 400)))

  ; status
  (p/with-style
    (p/stroke 255)
    (p/text (s/display-status) 400 (- (p/height) 400)))

  ; fade overlay
  (p/with-style
    (p/fill 0 (s/get-state :console-fade))
    (p/rect 0 0 (p/width) (p/height))))