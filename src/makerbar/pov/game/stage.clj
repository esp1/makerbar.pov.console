(ns makerbar.pov.game.stage
  (:require [makerbar.pov.mode :as mode]))

(def game-stage (atom nil))

(defn set-stage! [stage]
  (mode/init stage)
  (reset! game-stage stage))