(ns makerbar.pov.game.stage)

(def game-stage (atom nil))

(defn set-stage! [stage]
  (reset! game-stage stage))