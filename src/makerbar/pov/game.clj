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

(def button-coords
  {:north-west [-1 -1]
   :north      [0 -1]
   :north-east [1 -1]

   :west       [-1 0]
   :east       [1 0]

   :south-west [-1 1]
   :south      [0 1]
   :south-east [1 1]})

(defn rand-pattern
  "Returns a seq of random button ids. The length of the seq will at least 1 and at most num-buttons, but may be some number in between."
  [num-buttons]
  (let [button-ids (vec (keys button-coords))]
    (distinct
      (for [i (range num-buttons)]
        (get button-ids (rand-int 8))))))

(def arrow-angles
  {:north-west (* p/TAU -0.125)
   :north 0
   :north-east (* p/TAU 0.125)

   :west (* p/TAU -0.25)
   :east (* p/TAU 0.25)

   :south-west (* p/TAU -0.375)
   :south (* p/TAU 0.5)
   :south-east (* p/TAU 0.375)})

(defn draw-arrow [button-id]
  (p/with-matrix
    (p/rotate (get arrow-angles button-id))
    (p/shape [-5 10] [-5 0] [-10 0] [0 -10] [10 0] [5 0] [5 10])))

(defn draw-buttons
  [buttons]
  (let [r 10
        d (* r 2)
        delta (inc d)]
    (p/with-style
      (p/fill 255 255 0)
      (doseq [b buttons
              :let [[dx dy] (get button-coords b)]]
        (p/with-matrix
          (p/translate (* dx delta) (* dy delta))
          (draw-arrow b))))
    (p/with-style
      (p/stroke 255)
      (p/no-fill)
      (let [s (+ delta r)]
        (p/rect (- s) (- s) (* 2 s) (* 2 s))))))

;; Stages

(def game-state (atom nil))

(def initial-stage
  (reify UiMode

    (init [_]
      (reset! game-state {:ddr-a          {:feets 2
                                           :score 0}
                          :ddr-b          {:feets 2
                                           :score 0}
                          :target-pattern (rand-pattern 2)})
      (prn @game-state))

    (draw [_]
      ; clear
      (p/background 0)

      (p/with-style
        (p/stroke 255 0 0)
        (p/text (pr-str (:target-pattern @game-state)) 0 20))
      (p/with-matrix
        (p/translate [(/ 224 2) (/ 102 2)])
        (draw-buttons (:target-pattern @game-state))))

    (ddr-button-pressed [_ evt]
      (when (jaeger evt :north) (s/inc-pov-offset [0 -1]))
      (when (jaeger evt :south) (s/inc-pov-offset [0 1]))
      (when (jaeger evt :east) (s/inc-pov-offset [1 0]))
      (when (jaeger evt :west) (s/inc-pov-offset [-1 0]))
      (when (jaeger evt :north-west) (s/inc-img-scale 1))
      (when (jaeger evt :north-eats) (s/inc-img-scale -1)))

    (key-pressed [_ event]
      (condp = (.getKeyCode event)

        ;; DDR A
        KeyEvent/VK_Q #_north-west
        KeyEvent/VK_W #_north
        KeyEvent/VK_E #_north-east
        KeyEvent/VK_A #_west
        KeyEvent/VK_D #_east
        KeyEvent/VK_Z #_south-west
        KeyEvent/VK_X #_south
        KeyEvent/VK_C #_south-east

        ; DDR B
        KeyEvent/VK_I #_north-west
        KeyEvent/VK_O #_north
        KeyEvent/VK_P #_north-east
        KeyEvent/VK_K #_west
        KeyEvent/VK_SEMICOLON #_east
        KeyEvent/VK_COMMA #_south-west
        KeyEvent/VK_PERIOD #_south
        KeyEvent/VK_SLASH #_south-east
        KeyEvent/VK_SPACE (swap! game-state assoc-in [:target-pattern] (rand-pattern 2))

        nil))))

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
