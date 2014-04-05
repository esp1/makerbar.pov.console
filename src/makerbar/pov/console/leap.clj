(ns makerbar.pov.console.leap
  (:import [com.leapmotion.leap CircleGesture Controller Gesture Gesture$Type Listener SwipeGesture])
  (:require [clojure.core.async :as async :refer (<!! >!! go go-loop put!)]
            [clojure.math.numeric-tower :as math :refer (expt sqrt)]
            [makerbar.pov.console.images :as i]
            [makerbar.pov.console.state :as s]))


(def start-pan-zoom (atom nil))


(defn duration
  [frame0 frame1]
  (- (.timestamp frame1) (.timestamp frame0)))

(defn as-vec
  [v]
  [(.getX v) (.getY v) (.getZ v)])

(defn delta
  [v0 v1]
  [(- (.getX v1) (.getX v0))
   (- (.getY v1) (.getY v0))
   (- (.getZ v1) (.getZ v0))])

(defn distance
  [v0 v1]
  (sqrt (reduce + (map #(expt % 2) (map - v1 v0)))))

(defn hand-distance
  [h0 h1]
  (distance (as-vec (.palmPosition h0)) (as-vec (.palmPosition h1))))


; Console functions

(defn fade-console
  ([fade-in]
    (let [fade (s/get-state :console-fade)]
      (if fade-in
        (fade-console fade 0)
        (fade-console fade 200))))
  ([from to]
    (let [step (/ (- to from) 10)]
      (doseq [fade (range from (+ to step) step)]
        (s/set-state! :console-fade (float fade))
        (<!! (async/timeout 50))))))

(defn spin-image
  [dx]
  (s/set-state! :rotation-speed (Math/max -10 (Math/min 10 (+ (s/get-state :rotation-speed) dx)))))



(defn init-leap []
  (let [controller (Controller.)
        fade-ch (async/chan (async/sliding-buffer 1))
        listener (proxy [Listener] []
                   (onInit [controller]
                     (println "Leap initialized"))
                   (onConnect [controller]
                     (println "Leap connected")
                     (.enableGesture controller Gesture$Type/TYPE_SWIPE)
                     (let [config (.config controller)]
                       (if (and
                              (.setFloat config "Gesture.Swipe.MinLength" 200.0)
                              (.setFloat config "Gesture.Swipe.MinVelocity" 750.0))
                         (.save config))))
                   (onDisconnect [controller]
                     (println "Leap disconnected"))
                   (onExit [controller]
                     (println "Leap exited"))
                   (onFrame [controller]
                     (let [frame (.frame controller)
                           hands (.hands frame)
                           num-hands (.count hands)]
                       (if (> num-hands 0)
                         (put! fade-ch true)
                         (put! fade-ch false))
                       
                       (condp = num-hands
                         2 (let [left-hand (.leftmost hands)
                                 right-hand (.rightmost hands)]
                             (if (and
                                   (<= (-> left-hand .fingers .count) 1)
                                   (<= (-> right-hand .fingers .count) 1))
                               (if (nil? @start-pan-zoom)
                                 (reset! start-pan-zoom {:start-frame frame
                                                         :img-offset (s/get-state :img-offset)
                                                         :img-scale (s/get-state :img-scale)
                                                         :distance (hand-distance left-hand right-hand)})
                                 (let [{start-frame :start-frame
                                        [x0 y0] :img-offset
                                        s0 :img-scale
                                        d0 :distance} @start-pan-zoom
                                       [dx dy dz] (as-vec (.translation frame start-frame))
                                       [left-x left-y left-z] (as-vec (.palmPosition left-hand))
                                       [right-x right-y right-z] (as-vec (.palmPosition right-hand))
                                       box (.interactionBox frame)]
                                   (s/set-state! :img-offset [(+ x0 (* s/pov-width (/ dx (.width box)))) (+ y0 (- (* s/pov-height (/ dy (.height box)))))])
                                   (s/set-state! :img-scale (* s0 (/ (hand-distance left-hand right-hand) d0)))))
                               (reset! start-pan-zoom nil)))
                         1 (doseq [gesture (.gestures frame)]
                             (condp = (.type gesture)
                               Gesture$Type/TYPE_SWIPE (let [swipe (SwipeGesture. gesture)]
                                                         (if (> (.getX (.direction swipe)) 0)
                                                           (spin-image 1)
                                                           (spin-image -1)))))
                         nil))))
        control-ch (async/chan)]
    (.addListener controller listener)
    
    (go-loop [[val ch] (alts! [control-ch fade-ch])]
             (condp = ch
               fade-ch (do
                         (fade-console val)
                         (recur (alts! [control-ch fade-ch])))
               control-ch (.removeListener controller listener)))
    
    control-ch))
