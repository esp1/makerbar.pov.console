(ns makerbar.pov.console.control
    (:import [java.awt.event KeyEvent])
    (:require [makerbar.pov.console.images :as i]
              [makerbar.pov.console.state :as s]
              [makerbar.pov.console.util :as u]
              [quil.core :as q]))

(defn key-pressed []
  (let [factor (if #_shiftDown true 10 1)]
    (condp = (q/key-code)
      
      KeyEvent/VK_O (i/open-image-file)
      KeyEvent/VK_Z (i/inc-image-selection -1)
      KeyEvent/VK_X (i/inc-image-selection 1)
      KeyEvent/VK_SPACE (i/display-selected-image)
      
      KeyEvent/VK_LEFT (s/inc-pov-offset-x (- factor))
      KeyEvent/VK_RIGHT (s/inc-pov-offset-x factor)
      KeyEvent/VK_UP (s/inc-pov-offset-y (- factor))
      KeyEvent/VK_DOWN (s/inc-pov-offset-y factor)
      
      KeyEvent/VK_EQUALS (s/inc-img-scale factor)
      KeyEvent/VK_MINUS (s/inc-img-scale (- factor))
      
      KeyEvent/VK_H (s/inc-img-offset-x (- factor))
      KeyEvent/VK_K (s/inc-img-offset-x factor)
      KeyEvent/VK_U (s/inc-img-offset-y (- factor))
      KeyEvent/VK_J (s/inc-img-offset-y factor)
      
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

(defn display-controls []
  (u/with-style
    (q/stroke 255)
    (q/text "
l : re/load properties
o : open image file
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
            0 0)))
