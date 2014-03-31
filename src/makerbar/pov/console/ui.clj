(ns makerbar.pov.console.ui
  (:gen-class
    :extends processing.core.PApplet
    :methods [[captureEvent [processing.video.Capture] void]
              [movieEvent [processing.video.Movie] void]])
  (:import [processing.core PApplet])
  (:require [makerbar.pov.console.draw :as d]
            [makerbar.pov.console.images :as i]
            [makerbar.pov.console.kbd-control :as k]
            [makerbar.pov.console.processing :as p]
            [makerbar.pov.console.state :as s]))


(defn setup []
  (p/size (p/display-width) (p/display-height))
  ; (q/frame-rate 30)
  (d/init))

(def time-t (atom (System/currentTimeMillis)))

(defn draw []
  ; clear
  (p/background 100)
  
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
    (s/inc-pov-offset [(* (:rotation-speed @s/state) (:rotation-direction @s/state)) 0]))
  
  (p/with-matrix
    (p/translate 40 80)
    (p/scale 3)
    
    ; draw image
    (d/draw-image)
    
    (p/with-style
      (p/stroke 200)
      (p/no-fill)
      
      ; draw frame
      (p/rect -1 -1 (+ s/pov-width 1) (+ s/pov-height 1))))
  
  ; image list
  (p/with-matrix
    (p/translate (- (p/width) 500) 100)
    
    (p/with-matrix
      (when-let [img (i/get-selected-image)]
        (let [{:keys [offset scale]} (i/scale-image-instructions img s/pov-width s/pov-height)]
          (p/translate offset)
          (p/scale scale)
          (p/image img 0 0))))
    
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
  
  #_(x2-send))


(defn -setup [this] (p/with-applet this (setup)))
(defn -sketchFullScreen [this] true)
(defn -draw [this] (p/with-applet this (draw)))

(defn -keyPressed [this event] (p/with-applet this (k/key-pressed event)))

(defn -captureEvent [this camera] (.read camera))
(defn -movieEvent [this movie] (.read movie))

(defn -main [& args] (PApplet/main "makerbar.pov.console.ui"))
