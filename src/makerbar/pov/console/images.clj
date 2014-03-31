(ns makerbar.pov.console.images
  (:import [gifAnimation Gif]
           [javax.swing JFileChooser]
           [javax.swing.filechooser FileNameExtensionFilter]
           [processing.video Movie])
  (:require [clojure.java.io :as io]
            [clojure.string :as str]
            [makerbar.pov.console.processing :as p]
            [makerbar.pov.console.state :as s]))


(defn list-images
  [path]
  (mapv (fn [f] {:file f})
       (filterv #(.isFile %)
                (mapv #(io/file path %)
                     (-> (io/file path) .list sort)))))


(def image-list (atom (list-images "images")))
(def selected-image-index (atom 0))


(defmulti stop class)
(defmethod stop Gif [gif] (.stop gif))
(defmethod stop Movie [movie] (.stop movie))
(defmethod stop :default [this])


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

(defn display-image
  [img]
  (s/reset-settings)
  (stop (:image @s/state))
  (swap! s/state assoc :image img))

(defn get-image
  ([]
    (when (= (.showOpenDialog file-chooser (p/current-applet))
             JFileChooser/APPROVE_OPTION)
      (get-image (.getSelectedFile file-chooser))))
  ([index]
    (if-let [{:keys [file image] :as image-info} (get @image-list index)]
      (if image
        image
        (let [path (.getCanonicalPath file)
              suffix (str/lower-case (subs path (.lastIndexOf path ".")))
              img (condp = suffix
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
                      (p/load-image path)))]
          (swap! image-list assoc-in [index :image] img)
          img)))))

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
