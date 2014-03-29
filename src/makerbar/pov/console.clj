(ns makerbar.pov.console
  (:import [java.awt.event KeyEvent]
           [javax.swing JFileChooser]
           [javax.swing.filechooser FileNameExtensionFilter]
           [processing.core PImage])
  (:require [clojure.java.io :as io]
            [clojure.string :as str]
            [quil.applet :as a]
            [quil.core :as q]))


; Constants

(def pov-width (* 4 56))
(def pov-height (* 6 17))

(def x2-addr "192.168.0.3")

(def default-state {:brightness 0
                    :contrast 1
                    :pov-x-offset 0
                    :pov-y-offset 0})


; State

(def pg (atom nil))
(def img-pg (atom nil))

(def state (atom default-state))

(def file-chooser
  (let [image-file-filter (FileNameExtensionFilter. "Image/Movie file (png, jpg, bmp, gif, mov)" (into-array ["png" "jpg" "bmp" "gif" "mov"]))]
    (doto (JFileChooser. (io/file "images"))
      (.setAcceptAllFileFilterUsed false)
      (.addChoosableFileFilter image-file-filter)
      (.setFileFilter image-file-filter))))


; Utilities

(defmacro with-matrix
  [ & body]
  `(do
     (q/push-matrix)
     ~@body
     (q/pop-matrix)))

(defmacro with-style
  [ & body]
  `(do
     (q/push-style)
     ~@body
     (q/pop-style)))

(defn open-image-file []
  (when (= (.showOpenDialog file-chooser (a/current-applet))
           JFileChooser/APPROVE_OPTION)
    (let [file-path (.getCanonicalPath (.getSelectedFile file-chooser))
          suffix (str/lower-case (subs file-path (.lastIndexOf file-path ".")))]
      (condp = suffix
;        ".gif" (println "GIF" file-path)
        ".mov" (println "Movie file:" file-path)
        (do
          (println "Image file:" file-path)
          (swap! state assoc :image (q/load-image file-path)))))))

(defn wrapped
  ([value upper-bound] (wrapped value 0 upper-bound))
  ([value lower-bound upper-bound]
    {:pre (< lower-bound upper-bound)}
    (let [range (- upper-bound lower-bound)
          mval (mod value range)]
      (if (< mval 0) (+ mval range)
        (if (< range mval) (- mval range)
          mval)))))

(defn pov-offset-x
  [dx]
  (swap! state update-in [:pov-x-offset] #(wrapped (+ % dx))))

(defn pov-offset-y
  [dy]
  (swap! state update-in [:pov-y-offset] #(wrapped (+ % dx))))


; Drawing fns

(defn adjust-color
  "Apply contrast (*) and brightness (+) to color"
  [c contrast brightness]
  (letfn [(bounded [x] (min (max 0 x) 255))
          (adjust [x] (bounded (int (+ (* x contrast) brightness))))]
    (let [r (adjust (bit-and (bit-shift-right c 16) 0xff))
          g (adjust (bit-and (bit-shift-right c 8) 0xff))
          b (adjust (bit-and c 0xff))]
      (bit-or 0xff000000 (bit-shift-left r 16) (bit-shift-left g 8) b))))

(defn draw-image []
  (.beginDraw @pg)
  (.background @pg 0)
  (when-let [img (:image @state)]
    (println "got" img)
    ; adjust contrast and brightness
    (let [img-width (.width img)
          img-height (.height img)
          img-pixels (.pixels img)
          x-img (PImage. img-width img-height)]
      (.loadPixels img)
      (doseq [i (range 0 (* img-width img-height))]
        (let [c (aget img-pixels i)
              {:keys [contrast brightness]} @state]
          (aset img-pixels i (adjust-color c contrast brightness))))
      (.updatePixels img))
    
    ; flip image
    (when (:flip-image @state)
      (.scale @pg -1 1)
      (.translate @pg (- (- pov-width 1)) 0))
    
    ; draw image
    (let [min-x (if (> ))])
    )
  (.endDraw @pg)
  
  (q/image @pg 0 0))

(defn display-image-list [])
(defn display-instructions [])


; Processing fns

(defn setup []
  (q/frame-rate 10)
  (reset! pg (q/create-graphics pov-width pov-height))
  (reset! img-pg (q/create-graphics pov-width pov-height)))

(defn draw []
  ; clear
  (q/background 100)
  
  (with-matrix
    (q/translate 40 80)
    (q/scale 3 3)
    
    ; draw image
    (draw-image)
    
    (with-style
      (q/stroke 200)
      (q/no-fill)
      
      ; draw frame
      (q/rect -1 -1 (+ pov-width 1) (+ pov-height 1))))
  
  ; info
  (q/text (str "Display dimensions: " pov-width " x " pov-height) 40 (- 80 20 (q/text-descent)))
  
  ; image list
  (with-matrix
    (q/translate (- (q/width) 500) 100)
    (display-image-list))

  ; instructions 
  (with-matrix
    (q/translate 40 (- (q/height) 400))
    (display-instructions))
  
  #_(x2-send))

(q/defsketch POVConsole
  :title "Orbital Rendersphere Control"
  :setup setup
  :draw draw
  :size [(q/screen-width) (q/screen-height)]
  :key-pressed #(let [factor (if #_shiftDown true 1 10)]
                  (condp = (q/key-code)
                    KeyEvent/VK_O (open-image-file)
                    KeyEvent/VK_LEFT (pov-offset-x (- factor))
                    KeyEvent/VK_RIGHT (pov-offset-x factor)
                    KeyEvent/VK_UP (pov-offset-y (- factor))
                    KeyEvent/VK_DOWN (pov-offset-y factor)
                    nil)))
