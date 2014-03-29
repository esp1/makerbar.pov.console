(ns makerbar.pov.console.ui
  (:import [java.awt.event KeyEvent])
  (:require [makerbar.pov.console.draw :as d]
            [makerbar.pov.console.state :as s]
            [makerbar.pov.console.util :as u :refer (with-matrix with-style)]
            [quil.core :as q]))


; Processing fns

(defn setup []
  (q/frame-rate 10)
  (reset! s/graphics (q/create-graphics s/pov-width s/pov-height))
  (reset! s/img-graphics (q/create-graphics s/pov-width s/pov-height)))

(defn draw []
  ; clear
  (q/background 100)
  
  (with-matrix
    (q/translate 40 80)
    (q/scale 3 3)
    
    ; draw image
    (d/draw-image)
    
    (with-style
      (q/stroke 200)
      (q/no-fill)
      
      ; draw frame
      (q/rect -1 -1 (+ s/pov-width 1) (+ s/pov-height 1))))
  
  ; info
  (q/text (str "Display dimensions: " s/pov-width " x " s/pov-height) 40 (- 80 20 (q/text-descent)))
  
  ; image list
  (with-matrix
    (q/translate (- (q/width) 500) 100)
    (d/display-image-list))

  ; instructions 
  (with-matrix
    (q/translate 40 (- (q/height) 400))
    (d/display-instructions))
  
  #_(x2-send))

(q/defsketch POVConsole
  :title "Orbital Rendersphere Control"
  :setup setup
  :draw draw
  :size [400 300] #_[(q/screen-width) (q/screen-height)]
  :key-pressed #(let [factor (if #_shiftDown true 1 10)]
                  (condp = (q/key-code)
                    
                    KeyEvent/VK_O (s/open-image-file)
                    
                    KeyEvent/VK_LEFT (s/pov-offset-x (- factor))
                    KeyEvent/VK_RIGHT (s/pov-offset-x factor)
                    KeyEvent/VK_UP (s/pov-offset-y (- factor))
                    KeyEvent/VK_DOWN (s/pov-offset-y factor)
                    
                    nil)))
