(ns makerbar.pov.console.kbd-control
    (:import [java.awt.event KeyEvent])
    (:require [makerbar.pov.console.images :as i]
              [makerbar.pov.console.state :as s]))

(defn key-pressed [^KeyEvent event]
  (let [factor (if (.isShiftDown event) 1 10)]
    (condp = (.getKeyCode event)
      
      KeyEvent/VK_O (i/display-image (i/get-image))
      KeyEvent/VK_Z (i/inc-image-selection -1)
      KeyEvent/VK_X (i/inc-image-selection 1)
      KeyEvent/VK_SPACE (i/display-image (i/get-selected-image))
      
      KeyEvent/VK_C (i/capture-video)
      
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
      
      nil)))

(defn display-keyboard-controls []
"o : open image file
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
F : flip image")
