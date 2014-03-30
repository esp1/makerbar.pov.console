(ns makerbar.pov.console.ui
  (:require [makerbar.pov.console.control :as c]
            [makerbar.pov.console.draw :as d]
            [makerbar.pov.console.state :as s]
            [makerbar.pov.console.util :as u]
            [quil.core :as q]))


(defn setup []
;  (q/frame-rate 30)
  (d/init))

(def time-t (atom (System/currentTimeMillis)))

(defn draw []
  ; clear
  (q/background 100)
  
  ; info
  (q/text (str "Display dimensions: " s/pov-width " x " s/pov-height) 40 (- 80 20 (q/text-descent)))
  
  (let [t @time-t
        now (System/currentTimeMillis)
        fps (float (/ 1000 (- now t)))]
    (reset! time-t now)
    
    ; display frames per second
    (u/with-style
      (q/stroke 255)
      (q/text (str "Processing FPS: " (format "%.1f" fps)) 40 40))
    
    ; rotate
    (s/inc-pov-offset-x (* (:rotation-speed @s/state) (:rotation-direction @s/state))))
  
  (u/with-matrix
    (q/translate 40 80)
    (q/scale 3 3)
    
    ; draw image
    (d/draw-image)
    
    (u/with-style
      (q/stroke 200)
      (q/no-fill)
      
      ; draw frame
      (q/rect -1 -1 (+ s/pov-width 1) (+ s/pov-height 1))))
  
  ; image list
  (u/with-matrix
    (q/translate (- (q/width) 500) 100)
    (d/display-image-list))

  ; instructions 
  (u/with-matrix
    (q/translate 40 (- (q/height) 400))
    (c/display-controls))
  
  ; status
  (u/with-matrix
    (q/translate (- (q/width) 500) (- (q/height) 400))
    (s/display-status))
  
  #_(x2-send))

(q/defsketch POVConsole
  :title "Orbital Rendersphere Control"
  :setup setup
  :draw draw
  :size [(q/screen-width) (q/screen-height)]
  :key-pressed c/key-pressed)
