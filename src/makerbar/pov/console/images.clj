(ns makerbar.pov.console.images
  (:import [javax.swing JFileChooser]
           [javax.swing.filechooser FileNameExtensionFilter])
  (:require [clojure.java.io :as io]
            [clojure.string :as str]
            [makerbar.pov.console.state :as s]
            [makerbar.pov.console.util :as u]
            [quil.applet :as a]
            [quil.core :as q]))


(defn autoscale-image
  [img]
  (let [img-width (.width img)
        img-height (.height img)]
    (if (< (/ img-width img-height) (/ s/pov-width s/pov-height))
      (let [img-scale (float (/ s/pov-height img-height))]
        (swap! s/state assoc
               :img-scale img-scale
               :img-x-offset (int (/ (- s/pov-width (* img-width img-scale)) 2))))
      (let [img-scale (float (/ s/pov-width img-width))]
        (swap! s/state assoc
               :img-scale img-scale
               :img-y-offset (int (/ (- s/pov-height (* img-height img-scale)) 2))))))
  img)

(defn scale-image
  [img target-width target-height]
  (let [img-width (.width img)
        img-height (.height img)]
    (if (< (/ img-width img-height) (/ target-width target-height))
      (q/scale (/ target-height img-height))
      (q/scale (/ target-width img-width)))))

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
          (swap! s/state assoc :image (autoscale-image (q/load-image path))))))))


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
      (let [img (q/load-image (.getCanonicalPath file))]
        (scale-image img s/pov-width s/pov-height)
        (q/image img 0 0))))
  
  (u/with-matrix
    (q/translate 0 110)
    (u/with-style
      (q/stroke 255)
      (q/text (apply str (map #(str (.getName %) \newline) @image-list)) 0 0))))

(defn display-selected-image []
  (s/reset-settings)
  (open-image-file (selected-image-file)))
