(ns makerbar.pov.game
  (:require [clojure.core.async :as async :refer (go-loop)]
            [makerbar.pov.mode :refer (UiMode)]
            [makerbar.pov.state :as s]
            [makerbar.pov.ui.processing :as p]
            [makerbar.pov.ui.draw :as d]))

(def game-state (atom {:stage :initial}))

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

(defn init-game [ddr-ch]
  (when ddr-ch
    (go-loop []
      (when-let [evt (async/<! ddr-ch)]
        (when (jaeger evt :north) (s/inc-pov-offset [0 -1]))
        (when (jaeger evt :south) (s/inc-pov-offset [0 1]))
        (when (jaeger evt :east) (s/inc-pov-offset [1 0]))
        (when (jaeger evt :west) (s/inc-pov-offset [-1 0]))
        (when (jaeger evt :north-west) (s/inc-img-scale 1))
        (when (jaeger evt :north-eats) (s/inc-img-scale -1))
        (recur)))))

(defn rand-pattern [num-players]
  (distinct
    (for [i (range (* num-players 2))]
      (rand-int 8))))

(defmulti render :stage)
(defmethod render :initial [_]
  (p/with-style
    (p/fill 255 255 0)
    (p/text "Step on buttons!" 100 20)))

(def mode
  (reify UiMode

    (draw [_]
      ; clear
      (p/background 0)

      (d/pov-view #(render @game-state)))

    (key-pressed [_ event]
      (prn event))))
