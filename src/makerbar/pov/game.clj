(ns makerbar.pov.game
  (:import [java.awt.event KeyEvent])
  (:require [makerbar.pov.game.stage :as stage]
            [makerbar.pov.mode :as mode :refer (UiMode)]
            [makerbar.pov.state :as s]
            [makerbar.pov.ui.processing :as p]
            [makerbar.pov.ui.draw :as d]))

(defn jaeger
  "Returns true only if all target buttons are pressed on all ddr controllers.
  See: Pacific Rim movie."
  [{:keys [ddr-a ddr-b]} button-or-buttons]
  (let [buttons (if (seq? button-or-buttons)
                  button-or-buttons
                  [button-or-buttons])]
    (reduce #(and %1 %2)
            (concat (map #(get ddr-a %) buttons)
                    (map #(get ddr-b %) buttons)))))

(defn rand-pattern [num-players]
  (distinct
    (for [i (range (* num-players 2))]
      (rand-int 8))))

;; Stages

(def game-state (atom {:count 0}))

(def initial-stage
  (reify UiMode

    (init [_])

    (draw [_]
      (p/with-style
        (p/fill 0 255 255)
        (p/text (str "Revolution " (:count @game-state)) 0 20)))

    (ddr-button-pressed [_ evt]
      (when (jaeger evt :north) (s/inc-pov-offset [0 -1]))
      (when (jaeger evt :south) (s/inc-pov-offset [0 1]))
      (when (jaeger evt :east) (s/inc-pov-offset [1 0]))
      (when (jaeger evt :west) (s/inc-pov-offset [-1 0]))
      (when (jaeger evt :north-west) (s/inc-img-scale 1))
      (when (jaeger evt :north-eats) (s/inc-img-scale -1)))

    (key-pressed [_ event]
      (condp = (.getKeyCode event)

        KeyEvent/VK_UP (do (println "up") (swap! game-state update-in [:count] inc))
        KeyEvent/VK_DOWN (do (println "down") (swap! game-state update-in [:count] dec))

        nil)
      (prn @game-state event))))

;; Mode

(defn mode []
  (reify UiMode

    (init [_] (stage/set-stage! initial-stage))

    (draw [_]
      ; clear
      (p/background 0)

      (d/pov-view #(mode/draw @stage/game-stage)))

    (key-pressed [_ evt] (mode/key-pressed @stage/game-stage evt))

    (ddr-button-pressed [_ evt] (mode/ddr-button-pressed @stage/game-stage evt))))
