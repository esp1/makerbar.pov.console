(ns makerbar.pov.console
  (:require [makerbar.pov.console.keys :as k]
            [makerbar.pov.mode :refer (UiMode)]
            [makerbar.pov.state :as s]
            [makerbar.pov.ui.draw :as d]
            [makerbar.pov.ui.images :as img]
            [makerbar.pov.ui.processing :as p]))

(def time-t (atom (System/currentTimeMillis)))

;; Mode

(defn mode []
  (reify UiMode

    (init [_])

    (draw [_]
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

      (when-let [img (s/get-state :image)]
        (when (and (< 0 (.width img)) (< 0 (.height img)))
          (p/with-matrix
            ;    (p/translate 40 80)
            ;    (p/scale 3)

            ; draw scaled image to pov-graphics
            (d/pov-view
              #(let [{:keys [offset scale]} (img/scale-image-instructions)]
                (p/background 0)
                (p/scale scale)
                (p/image img offset))))))

      ; image list
      (p/with-matrix
        (p/translate (- (p/width) 500) 100)

        #_(p/with-matrix
            (when-let [img (img/get-selected-image)]
              (let [{:keys [offset scale]} (img/scale-image-instructions (.width img) (.height img) s/pov-width s/pov-height)]
                (p/scale scale)
                (p/image img offset))))

        (p/with-style
          (p/stroke 255)
          (p/text (img/display-image-list) 0 120)))

      ; instructions
      (p/with-style
        (p/stroke 255)
        (p/text (k/keyboard-controls) 40 (- (p/height) 400)))

      ; status
      (p/with-style
        (p/stroke 255)
        (p/text (s/display-status) 400 (- (p/height) 400)))

      ; fade overlay
      (p/with-style
        (p/fill 0 (s/get-state :console-fade))
        (p/rect 0 0 (p/width) (p/height))))

    (key-pressed [_ event] (k/key-pressed event))))
