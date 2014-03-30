(ns makerbar.pov.console.state
  (:require [makerbar.pov.console.util :as u]
            [quil.core :as q]))


; Constants

(def pov-width (* 4 56))
(def pov-height (* 6 17))

(def x2-addr "192.168.0.3")

(def default-state
  {:brightness 0
   :contrast 1
                    
   :pov-x-offset 0
   :pov-y-offset 0
                    
   :img-x-offset 0
   :img-y-offset 0
   :img-scale 1
                    
   :rotation-direction 1
   :rotation-speed 2
   :flip-image false})


; State

(def state (atom default-state))


; Functions

(defn wrapped
  ([value upper-bound] (wrapped value 0 upper-bound))
  ([value lower-bound upper-bound]
    {:pre (< lower-bound upper-bound)}
    (let [range (- upper-bound lower-bound)
          mval (mod value range)]
      (if (< mval 0) (+ mval range)
        (if (< range mval) (- mval range)
          mval)))))

(defn inc-pov-offset-x [dx] (swap! state update-in [:pov-x-offset] #(wrapped (+ % dx) pov-width)))
(defn inc-pov-offset-y [dy] (swap! state update-in [:pov-y-offset] #(wrapped (+ % dy) pov-height)))

(defn inc-img-scale [ds] (swap! state update-in [:img-scale] #(+ % (* 0.01 ds))))

(defn inc-img-offset-x [dx] (swap! state update-in [:img-x-offset] #(+ % dx)))
(defn inc-img-offset-y [dy] (swap! state update-in [:img-y-offset] #(+ % dy)))

(defn rotation-speed [speed] (swap! state assoc :rotation-speed speed))
(defn toggle-rotation-direction [] (swap! state assoc :rotation-direction (- (:rotation-direction @state))))

(defn toggle-flip-image []
  (swap! state assoc :flip-image (not (:flip-image @state)))
  (toggle-rotation-direction))

(defn inc-brightness [db] (swap! state update-in [:brightness] #(+ % db)))
(defn inc-contrast [dc] (swap! state update-in [:contrast] #(max 1 (+ % dc))))

(defn reset-settings [] (reset! state default-state))

(defn display-status []
  (u/with-style
    (q/stroke 255)
    (q/text (apply str (map (fn [[key val]]
                              (str (name key) ": " val \newline))
                            @state))
            0 0)))
