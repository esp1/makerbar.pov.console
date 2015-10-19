(ns makerbar.pov.console
  (:import [java.awt.event KeyEvent])
  (:require [makerbar.pov.controller.ddr :as ddr]
            [makerbar.pov.mode :refer (UiMode)]
            [makerbar.pov.state :as s]
            [makerbar.pov.ui.draw :as d]
            [makerbar.pov.ui.images :as img]
            [makerbar.pov.ui.processing :as p]
            [makerbar.pov.mode :as mode]))

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
        (p/text "o : open image file
                 c : capture video

                 z/x : select image
                 space : display selected image

                 -/+ : scale image decrease/increase
                 H/K/U/J : image offset left/right/up/down
                 arrow keys : globe offset
                 A/D : contrast decrease/increase
                 S/W : brightness decrease/increase
                 (hold shift for fine scale/offset)
                 0-9 : rotation speed
                 R : change rotation direction
                 F : flip image"
                40 (- (p/height) 400)))

      ; status
      (p/with-style
        (p/stroke 255)
        (p/text (s/display-status) 400 (- (p/height) 400)))

      ; fade overlay
      (p/with-style
        (p/fill 0 (s/get-state :console-fade))
        (p/rect 0 0 (p/width) (p/height))))

    (key-pressed [_ event]
      (let [factor (if (.isShiftDown event) 1 10)]
        (condp = (.getKeyCode event)

          KeyEvent/VK_O (img/display-image (img/get-image))
          KeyEvent/VK_Z (img/inc-image-selection -1)
          KeyEvent/VK_X (img/inc-image-selection 1)
          KeyEvent/VK_SPACE (img/display-image (img/get-selected-image))

          KeyEvent/VK_C (img/capture-video)

          KeyEvent/VK_LEFT (s/inc-pov-offset [(- factor) 0])
          KeyEvent/VK_RIGHT (s/inc-pov-offset [factor 0])
          KeyEvent/VK_UP (s/inc-pov-offset [0 (- factor)])
          KeyEvent/VK_DOWN (s/inc-pov-offset [0 factor])

          KeyEvent/VK_EQUALS (s/inc-img-scale factor)
          KeyEvent/VK_MINUS (s/inc-img-scale (- factor))

          KeyEvent/VK_H (s/inc-img-offset [(- factor) 0])
          KeyEvent/VK_K (s/inc-img-offset [factor 0])
          KeyEvent/VK_U (s/inc-img-offset [0 (- factor)])
          KeyEvent/VK_J (s/inc-img-offset [0 factor])

          KeyEvent/VK_0 (s/rotation-speed 0)
          KeyEvent/VK_1 (s/rotation-speed 1)
          KeyEvent/VK_2 (s/rotation-speed 2)
          KeyEvent/VK_3 (s/rotation-speed 3)
          KeyEvent/VK_4 (s/rotation-speed 4)
          KeyEvent/VK_5 (s/rotation-speed 5)
          KeyEvent/VK_6 (s/rotation-speed 6)
          KeyEvent/VK_7 (s/rotation-speed 7)
          KeyEvent/VK_8 (s/rotation-speed 8)
          KeyEvent/VK_9 (s/rotation-speed 9)
          KeyEvent/VK_R (s/toggle-rotation-direction)

          KeyEvent/VK_F (s/toggle-flip-image)

          KeyEvent/VK_S (s/inc-brightness (- factor))
          KeyEvent/VK_W (s/inc-brightness factor)

          KeyEvent/VK_A (s/inc-contrast (- factor))
          KeyEvent/VK_D (s/inc-contrast factor)

          KeyEvent/VK_ESCAPE (s/reset-settings)

          KeyEvent/VK_G (mode/->mode :game)

          nil)))
    (key-released [_ event])

    (ddr-button-pressed [_ evt]
      (when (ddr/jaeger evt :north) (s/inc-pov-offset [0 -1]))
      (when (ddr/jaeger evt :south) (s/inc-pov-offset [0 1]))
      (when (ddr/jaeger evt :east) (s/inc-pov-offset [1 0]))
      (when (ddr/jaeger evt :west) (s/inc-pov-offset [-1 0]))

      (when (ddr/jaeger evt :north-west) (s/inc-img-scale 1))
      (when (ddr/jaeger evt :north-east) (s/inc-img-scale -1))

      #_(when (ddr/jaeger evt :south-west) (img/inc-image-selection -1))
      #_(when (ddr/jaeger evt :south-east) (img/inc-image-selection 1)))))
