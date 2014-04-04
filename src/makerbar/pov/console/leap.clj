(ns makerbar.pov.console.leap
  (:import [com.leapmotion.leap Controller Gesture Gesture$Type Listener])
  (:require [clojure.core.async :as async :refer (<!! >!! go go-loop put!)]
            [makerbar.pov.console.images :as i]
            [makerbar.pov.console.state :as s]))

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

(defn init-leap []
  (let [controller (Controller.)
        fade-ch (async/chan (async/sliding-buffer 1))
        listener (proxy [Listener] []
                   (onInit [controller]
                     (println "Leap initialized"))
                   (onConnect [controller]
                     (println "Leap connected")
                     (.enableGesture controller Gesture$Type/TYPE_SWIPE)
                     (.enableGesture controller Gesture$Type/TYPE_CIRCLE)
                     (.enableGesture controller Gesture$Type/TYPE_SCREEN_TAP)
                     (.enableGesture controller Gesture$Type/TYPE_KEY_TAP))
                   (onDisconnect [controller]
                     (println "Leap disconnected"))
                   (onExit [controller]
                     (println "Leap exited"))
                   (onFrame [controller]
                     (let [frame (.frame controller)]
                       (let [hands (.hands frame)]
                         (if (< 0 (.count hands))
                           (put! fade-ch true)
                           (put! fade-ch false)))
                       #_(for [gesture (.gestures frame)]
                          (condp = (.type gesture)
                            Gesture$Type/TYPE_SWIPE
                            Gesture$Type/TYPE_CIRCLE
                            Gesture$Type/TYPE_SCREEN_TAP
                            Gesture$Type/TYPE_KEY_TAP)))))
        control-ch (async/chan)]
    (.addListener controller listener)
    
    (go-loop [[val ch] (alts! [control-ch fade-ch])]
             (condp = ch
               fade-ch (do
                         (fade-console val)
                         (recur (alts! [control-ch fade-ch]))))
             control-ch (.removeListener controller listener))
    
    control-ch))
