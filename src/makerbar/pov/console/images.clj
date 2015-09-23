(ns makerbar.pov.console.images
  (:import [gifAnimation Gif]
           [javax.swing JFileChooser JOptionPane]
           [javax.swing.filechooser FileNameExtensionFilter]
           [processing.core PConstants]
           [processing.video Capture Movie])
  (:require [clojure.java.io :as io]
            [clojure.string :as str]
            [makerbar.pov.console.processing :as p]
            [makerbar.pov.console.state :as s]))


(defn image-file? [f]
  (when (.isFile f)
    (let [extension (-> f .getName (str/replace #"^.*\." "") .toLowerCase)]
      (some #{extension} ["gif" "jpg" "mov" "png"]))))

(defn list-images
  [path]
  (mapv (fn [f] {:file f})
       (filterv #(image-file? %)
                (mapv #(io/file path %)
                     (-> (io/file path) .list sort)))))


(def image-list (atom (list-images "images")))
(def selected-image-index (atom 0))


(defmulti stop class)
(defmethod stop Capture [camera] (.stop camera))
(defmethod stop Gif [gif] (.stop gif))
(defmethod stop Movie [movie] (.stop movie))
(defmethod stop :default [this])


(defn scale-image-instructions
  "Returns a map indicating the offset and scaling factor that should be used to scale and center the given image into the target dimensions."
  ([]
    (let [{:keys [img-width img-height]} @s/state]
      (scale-image-instructions img-width img-height s/pov-width s/pov-height)))
  ([img-width img-height target-width target-height]
    {:pre [(< 0 img-width)
           (< 0 img-height)
           (< 0 target-width)
           (< 0 target-height)]}
    (if (< (/ img-width img-height) (/ target-width target-height))
      (let [img-scale (/ target-height img-height)]
        {:offset [(int (/ (- target-width (* img-width img-scale)) 2)) 0]
         :scale img-scale})
      (let [img-scale (/ target-width img-width)]
        {:offset [0 (int (/ (- target-height (* img-height img-scale)) 2))]
         :scale img-scale}))))

(def file-chooser
  (let [image-file-filter (FileNameExtensionFilter. "Image/Movie file (png, jpg, bmp, gif, mov)" (into-array ["png" "jpg" "bmp" "gif" "mov"]))]
    (doto (JFileChooser. (io/file "images"))
      (.setAcceptAllFileFilterUsed false)
      (.addChoosableFileFilter image-file-filter)
      (.setFileFilter image-file-filter))))

(defn display-image
  [img]
  (when img
    (stop (s/get-state :image))
    
    (s/reset-settings)
    
    (s/set-state! :image img)
    
    (let [img-width (.width img)
          img-height (.height img)]
      (if (and (< 0 img-width) (< 0 img-height))
        (s/set-state!
          :img-width img-width
          :img-height img-height)))))

(defn get-image
  ([]
    (when (= (.showOpenDialog file-chooser (p/current-applet))
             JFileChooser/APPROVE_OPTION)
      (get-image (.getSelectedFile file-chooser))))
  ([file]
    (let [path (.getCanonicalPath file)
          suffix (str/lower-case (subs path (.lastIndexOf path ".")))]
      (condp = suffix
        ".gif" (do
                 (println "GIF" path)
                 (let [gif (Gif. (p/current-applet) path)]
                   (.loop gif)
                   gif))
        ".mov" (do
                 (println "Movie file:" path)
                 (let [movie (Movie. (p/current-applet) path)]
                   (.loop movie)
                   movie))
        (do
          (println "Image file:" path)
          (p/load-image path))))))

(defn get-selected-image []
  (if-let [{:keys [file image] :as image-info} (get @image-list @selected-image-index)]
    (if image
      image
      (let [img (get-image file)]
        (swap! image-list assoc-in [@selected-image-index :image] img)
        img))))

(defn inc-image-selection
  [di]
  (reset! selected-image-index (let [n (+ @selected-image-index di)
                                     min 0
                                     max (- (count @image-list) 1)]
                                 (if (< n min) max
                                   (if (< max n) min
                                     n)))))

(defn display-image-list []
  (apply str (for [i (range (count @image-list))]
               (str (.getName (:file (get @image-list i)))
                    (if (= i @selected-image-index) " *")
                    \newline))))


; video

(defn list-cameras []
  (p/cursor PConstants/WAIT)
  (let [cameras (Capture/list)]
    (p/cursor PConstants/ARROW)
    cameras))

(defn capture-video []
  (let [cameras (list-cameras)
        selected-camera (JOptionPane/showInputDialog
                          (p/current-applet)
                          ""
                          "Select camera"
                          JOptionPane/PLAIN_MESSAGE
                          nil
                          cameras
                          nil)]
    (when selected-camera
      (println "Opening camera" selected-camera)
      
      (let [camera-props (apply merge (for [[key val] (map #(str/split % #"=")
                                                           (str/split selected-camera #","))]
                                        {(keyword key) val}))
            {size :size} camera-props
            [width height] (str/split size #"x")]
        (s/set-state!
          :image-width (Integer/valueOf width)
          :image-height (Integer/valueOf height))
        
        (let [camera (Capture. (p/current-applet) selected-camera)]
          (.start camera)
          (display-image camera))))))
