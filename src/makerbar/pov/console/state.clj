(ns makerbar.pov.console.state
  (:import [javax.swing JFileChooser]
           [javax.swing.filechooser FileNameExtensionFilter])
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
                    :pov-y-offset 0
                    
                    :img-x-offset 0
                    :img-y-offset 0
                    :img-scale 1})


; State

(def graphics (atom nil))
(def img-graphics (atom nil))

(def state (atom default-state))


; Functions

(def file-chooser
  (let [image-file-filter (FileNameExtensionFilter. "Image/Movie file (png, jpg, bmp, gif, mov)" (into-array ["png" "jpg" "bmp" "gif" "mov"]))]
    (doto (JFileChooser. (io/file "images"))
      (.setAcceptAllFileFilterUsed false)
      (.addChoosableFileFilter image-file-filter)
      (.setFileFilter image-file-filter))))

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
  (swap! state update-in [:pov-x-offset] #(wrapped (+ % dx) pov-width)))

(defn pov-offset-y
  [dy]
  (swap! state update-in [:pov-y-offset] #(wrapped (+ % dy) pov-height)))
