(ns makerbar.pov.console.leap
  (:import [com.leapmotion.leap Controller Gesture Gesture$Type Listener])
  (:require [clojure.core.async :as async :refer (<! chan go)]
            [makerbar.pov.console.images :as i]
            [makerbar.pov.console.state :as s]))

(defn init-leap []
  (let [controller (Controller.)
        listener (proxy [Listener] []
                   (onConnect [controller]
                     (.enableGesture controller Gesture$Type/TYPE_SWIPE)
                     (.enableGesture controller Gesture$Type/TYPE_CIRCLE)
                     (.enableGesture controller Gesture$Type/TYPE_SCREEN_TAP)
                     (.enableGesture controller Gesture$Type/TYPE_KEY_TAP))
                   (onFrame [controller]
                     (let [frame (.frame controller)]
                       (println (str "Frame id: " (.id frame)
                                     ", timestamp: " (.timestamp frame)
                                     ", hands: " (-> frame .hands .count)
                                     ", fingers: " (-> frame .fingers .count)
                                     ", tools: " (-> frame .tools .count)
                                     ", gestures " (-> frame .gestures .count))))))
        control-ch (chan)]
    (.addListener controller listener)

    (go
      (<! control-ch)
      (.removeListener controller listener))

    control-ch))
