(ns makerbar.pov.console.ui
  (:gen-class
    :extends processing.core.PApplet
    :methods [[captureEvent [processing.video.Capture] void]
              [movieEvent [processing.video.Movie] void]])
  (:import [processing.core PApplet])
  (:require [clojure.tools.cli :as cli]
            [makerbar.pov.console.controller.ddr :as ddr]
            [makerbar.pov.console.controller.keyboard :as k]
            [makerbar.pov.console.draw :as d]
            [makerbar.pov.console.game :as game]
            [makerbar.pov.console.images :as i]
            [makerbar.pov.console.net :as n]
            [makerbar.pov.console.processing :as p]
            [makerbar.pov.console.state :as s]))


(defn setup []
  (p/size (p/display-width) (p/display-height))
  ; (q/frame-rate 30)
  (d/init))

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


(defn -setup [this] (p/with-applet this (setup)))
;(defn -sketchFullScreen [this] true)
(defn -draw [this] (p/with-applet this (draw)))

(defn -keyPressed [this event] (p/with-applet this (k/key-pressed event)))

(defn -captureEvent [this camera] (.read camera))
(defn -movieEvent [this movie] (.read movie))

(defn -main
  [& args]

  (let [{{:keys [host port mirror]} :options}
        (cli/parse-opts args
                        [["-h" "--host HOST" "Host IP address"]
                         ["-p" "--port PORT" "Port number"
                          :default 10000
                          :parse-fn #(Integer/parseInt %)]
                         ["-m" "--mirror" "Mirror console display"]])]
    (if host
      (do
        (println "Connecting to Rendersphere at" (str host ":" port))
        (s/set-pov-addr! {:host host
                          :port port}))
      (println "Rendersphere connection not configured"))
    (if mirror (s/set-state! :console-mirror mirror))

    (let [ch (ddr/init-ddr)]
      (game/init-game ch)))

  (PApplet/main "makerbar.pov.console.ui"))
