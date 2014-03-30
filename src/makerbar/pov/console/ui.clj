(ns makerbar.pov.console.ui
  (:gen-class)
  (:require [makerbar.pov.console.draw :as d]
            [makerbar.pov.console.images :as i]
            [makerbar.pov.console.kbd-control :as k]
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
    (s/inc-pov-offset [(* (:rotation-speed @s/state) (:rotation-direction @s/state)) 0]))
  
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
    
    (u/with-matrix
     (when-let [img (i/get-image @i/selected-image-index)]
       (let [{:keys [offset scale]} (i/scale-image-instructions img s/pov-width s/pov-height)]
         (q/translate offset)
         (q/scale scale)
         (q/image img 0 0))))
    
    (u/with-style
      (q/stroke 255)
      (q/text (i/display-image-list) 0 120)))

  ; instructions 
  (u/with-style
    (q/stroke 255)
    (q/text (k/display-keyboard-controls) 40 (- (q/height) 400)))
  
  ; status
  (u/with-style
    (q/stroke 255)
    (q/text (s/display-status) 400 (- (q/height) 400)))
  
  #_(x2-send))

(defn -main [& args]
  (q/sketch
    :title "Orbital Rendersphere Control"
    :setup setup
    :draw draw
    :size [(q/screen-width) (q/screen-height)]
    :key-pressed k/key-pressed
    :on-close #(System/exit 0)))
