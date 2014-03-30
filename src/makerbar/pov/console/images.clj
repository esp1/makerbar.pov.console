(ns makerbar.pov.console.images
  (:import [javax.swing JFileChooser]
           [javax.swing.filechooser FileNameExtensionFilter])
  (:require [clojure.java.io :as io]
            [clojure.string :as str]
            [makerbar.pov.console.state :as s]
            [makerbar.pov.console.util :as u]
            [quil.applet :as a]
            [quil.core :as q]))


(defn scale-image-instructions
  "Returns a map indicating the offset and scaling factor that should be used to scale and center the given image into the target dimensions."
  [img target-width target-height]
  (let [img-width (.width img)
        img-height (.height img)]
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

(defn open-image-file
  ([]
    (when (= (.showOpenDialog file-chooser (a/current-applet))
             JFileChooser/APPROVE_OPTION)
      (open-image-file (.getSelectedFile file-chooser))))
  ([file]
    (let [path (.getCanonicalPath file)
          suffix (str/lower-case (subs path (.lastIndexOf path ".")))]
      (condp = suffix
        ; ".gif" (println "GIF" file-path)
        ".mov" (println "Movie file:" path)
        (do
          (println "Image file:" path)
          (swap! s/state assoc :image (q/load-image path)))))))


(defn list-images
  [path]
  (filterv #(.isFile %)
          (map #(io/file path %)
               (-> (io/file path) .list sort))))

(def image-list (atom (list-images "images")))
(def selected-image-index (atom 0))

(defn selected-image-file []
  (get @image-list @selected-image-index))

(defn inc-image-selection
  [di]
  (reset! selected-image-index (let [n (+ @selected-image-index di)
                                     min 0
                                     max (- (count @image-list) 1)]
                                 (if (< n min) max
                                   (if (< max n) min
                                     n)))))

(defn display-image-list []
  (u/with-matrix
    (when-let [file (selected-image-file)]
      (let [img (q/load-image (.getCanonicalPath file))
            {:keys [offset scale]} (scale-image-instructions img s/pov-width s/pov-height)]
        (q/translate offset)
        (q/scale scale)
        (q/image img 0 0))))
  
  (u/with-matrix
    (q/translate 0 110)
    (u/with-style
      (q/stroke 255)
      (q/text (apply str (map #(str (.getName %) \newline) @image-list)) 0 0))))

(defn display-selected-image []
  (s/reset-settings)
  (open-image-file (selected-image-file)))
