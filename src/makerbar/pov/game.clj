(ns makerbar.pov.game
  (:import [java.awt.event KeyEvent])
  (:require [makerbar.pov.game.stage :as stage]
            [makerbar.pov.mode :as mode :refer (UiMode)]
            [makerbar.pov.state :as s]
            [makerbar.pov.ui.processing :as p]
            [makerbar.pov.ui.draw :as d]
            [makerbar.pov.console :as console]))

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
    (apply hash-set
           (for [i (range num-buttons)]
             (get button-ids (rand-int 8))))))

(def arrow-angles
  {:north-west (* p/TAU -0.125)
   :north      0
   :north-east (* p/TAU 0.125)

   :west       (* p/TAU -0.25)
   :east       (* p/TAU 0.25)

   :south-west (* p/TAU -0.375)
   :south      (* p/TAU 0.5)
   :south-east (* p/TAU 0.375)})

(defn draw-arrow [button-id]
  (p/with-matrix
    (p/rotate (get arrow-angles button-id))
    (p/shape [-5 10] [-5 0] [-10 0] [0 -10] [10 0] [5 0] [5 10])))

(defn draw-x []
  (p/with-matrix
    (p/rotate (/ p/TAU 8))
    (p/shape [-10 -4] [-4 -4] [-4 -10] [4 -10] [4 -4] [10 -4]
             [10 4] [4 4] [4 10] [-4 10] [-4 4] [-10 4])))

(def button-size 20)
(def button-spacing button-size)

(defn draw-button-glyphs
  [buttons glyph-fn]
  (doseq [b buttons
          :let [[dx dy] (get button-coords b)]]
    (p/with-matrix
      (p/translate (* dx button-spacing) (* dy button-spacing))
      (glyph-fn b))))

(defn draw-button-border []
  (let [s (+ button-spacing (/ button-size 2) 2)]
    (p/rect (- s) (- s) (* 2 s) (* 2 s))))

;; Stages

(def score-range 10)
(def game-state (atom nil))

(defn get-buttons [team-map]
  (->> (:buttons team-map)
       (filter second)
       (map first)
       (apply hash-set)))

(defn new-pattern []
  (swap! game-state assoc-in [:pattern] (rand-pattern (:feets @game-state))))

(defn point->a []
  (swap! game-state update-in [:score]
         #(if (< % score-range)
           (inc %)
           %))
  (new-pattern))
(defn point->b []
  (swap! game-state update-in [:score]
         #(if (> % 0)
           (dec %)
           %))
  (new-pattern))

(defn update-game-state []
  (let [a-buttons (get-buttons (:team-a @game-state))
        b-buttons (get-buttons (:team-b @game-state))
        pattern (:pattern @game-state)]
    (cond
      (= a-buttons pattern) (point->a)
      (= b-buttons pattern) (point->b))))

(def initial-stage
  (reify UiMode

    (init [_]
      (reset! game-state {:feets   2
                          :score   (/ score-range 2)
                          :team-a  {}
                          :team-b  {}})
      (new-pattern))

    (draw [_]
      ; clear
      (p/background 0)

      ; draw score
      (let [pct (/ (:score @game-state) score-range)
            a-height (* pct 102)
            b-height (- 102 a-height)]
        ; team A score
        (p/with-style
          (p/stroke 0 0 255 128)
          (p/fill 0 0 255 128)
          (p/rect 0 0 224 a-height))
        ; team B score
        (p/with-style
          (p/stroke 255 0 0 128)
          (p/fill 255 0 0 128)
          (p/rect 0 a-height 224 b-height)))

      ; draw button patterns
      (p/with-matrix
        (p/translate [(/ 224 2) (/ 102 2)])

        ; draw button border
        (p/with-matrix
          (p/stroke 255)
          (p/fill 0)
          (draw-button-border))

        (let [pattern (get-in @game-state [:pattern])
              a-buttons (get-buttons (:team-a @game-state))
              b-buttons (get-buttons (:team-b @game-state))]
          ; draw team A buttons
          (p/with-style
            (p/fill 0 0 255)
            (draw-button-glyphs a-buttons (fn [b]
                                            (if (some #{b} pattern)
                                              (draw-arrow b)
                                              (draw-x)))))

          ; draw team B buttons
          (p/with-style
            (p/fill 255 0 0)
            (draw-button-glyphs b-buttons (fn [b]
                                            (if (some #{b} pattern)
                                              (draw-arrow b)
                                              (draw-x)))))

          ; draw target pattern
          (p/with-style
            (p/stroke 255)
            (p/no-fill)
            (draw-button-glyphs pattern draw-arrow)))))

    (ddr-button-pressed [_ evt]
      (swap! game-state assoc-in [:team-a :buttons :north-west] (get-in evt [:ddr-a :north-west]))
      (swap! game-state assoc-in [:team-a :buttons :north] (get-in evt [:ddr-a :north]))
      (swap! game-state assoc-in [:team-a :buttons :north-east] (get-in evt [:ddr-a :north-east]))
      (swap! game-state assoc-in [:team-a :buttons :west] (get-in evt [:ddr-a :west]))
      (swap! game-state assoc-in [:team-a :buttons :east] (get-in evt [:ddr-a :east]))
      (swap! game-state assoc-in [:team-a :buttons :south-west] (get-in evt [:ddr-a :south-west]))
      (swap! game-state assoc-in [:team-a :buttons :south] (get-in evt [:ddr-a :south]))
      (swap! game-state assoc-in [:team-a :buttons :south-east] (get-in evt [:ddr-a :south-east]))

      (swap! game-state assoc-in [:team-b :buttons :north-west] (get-in evt [:ddr-b :north-west]))
      (swap! game-state assoc-in [:team-b :buttons :north] (get-in evt [:ddr-b :north]))
      (swap! game-state assoc-in [:team-b :buttons :north-east] (get-in evt [:ddr-b :north-east]))
      (swap! game-state assoc-in [:team-b :buttons :west] (get-in evt [:ddr-b :west]))
      (swap! game-state assoc-in [:team-b :buttons :east] (get-in evt [:ddr-b :east]))
      (swap! game-state assoc-in [:team-b :buttons :south-west] (get-in evt [:ddr-b :south-west]))
      (swap! game-state assoc-in [:team-b :buttons :south] (get-in evt [:ddr-b :south]))
      (swap! game-state assoc-in [:team-b :buttons :south-east] (get-in evt [:ddr-b :south-east])))

    (key-pressed [_ event]
      (condp = (.getKeyCode event)

        KeyEvent/VK_ESCAPE (mode/->mode :console)

        KeyEvent/VK_EQUALS (s/inc-img-scale 10)
        KeyEvent/VK_MINUS (s/inc-img-scale -10)

        KeyEvent/VK_LEFT (s/inc-pov-offset [-10 0])
        KeyEvent/VK_RIGHT (s/inc-pov-offset [10 0])
        KeyEvent/VK_UP (s/inc-pov-offset [0 -10])
        KeyEvent/VK_DOWN (s/inc-pov-offset [0 10])

        ;; team A
        KeyEvent/VK_Q (swap! game-state assoc-in [:team-a :buttons :north-west] true)
        KeyEvent/VK_W (swap! game-state assoc-in [:team-a :buttons :north] true)
        KeyEvent/VK_E (swap! game-state assoc-in [:team-a :buttons :north-east] true)
        KeyEvent/VK_A (swap! game-state assoc-in [:team-a :buttons :west] true)
        KeyEvent/VK_D (swap! game-state assoc-in [:team-a :buttons :east] true)
        KeyEvent/VK_Z (swap! game-state assoc-in [:team-a :buttons :south-west] true)
        KeyEvent/VK_X (swap! game-state assoc-in [:team-a :buttons :south] true)
        KeyEvent/VK_C (swap! game-state assoc-in [:team-a :buttons :south-east] true)

        ; team B
        KeyEvent/VK_I (swap! game-state assoc-in [:team-b :buttons :north-west] true)
        KeyEvent/VK_O (swap! game-state assoc-in [:team-b :buttons :north] true)
        KeyEvent/VK_P (swap! game-state assoc-in [:team-b :buttons :north-east] true)
        KeyEvent/VK_K (swap! game-state assoc-in [:team-b :buttons :west] true)
        KeyEvent/VK_SEMICOLON (swap! game-state assoc-in [:team-b :buttons :east] true)
        KeyEvent/VK_COMMA (swap! game-state assoc-in [:team-b :buttons :south-west] true)
        KeyEvent/VK_PERIOD (swap! game-state assoc-in [:team-b :buttons :south] true)
        KeyEvent/VK_SLASH (swap! game-state assoc-in [:team-b :buttons :south-east] true)

        KeyEvent/VK_SPACE (new-pattern)

        nil)

      (update-game-state))

    (key-released [_ event]
      (condp = (.getKeyCode event)

        ;; team A
        KeyEvent/VK_Q (swap! game-state assoc-in [:team-a :buttons :north-west] false)
        KeyEvent/VK_W (swap! game-state assoc-in [:team-a :buttons :north] false)
        KeyEvent/VK_E (swap! game-state assoc-in [:team-a :buttons :north-east] false)
        KeyEvent/VK_A (swap! game-state assoc-in [:team-a :buttons :west] false)
        KeyEvent/VK_D (swap! game-state assoc-in [:team-a :buttons :east] false)
        KeyEvent/VK_Z (swap! game-state assoc-in [:team-a :buttons :south-west] false)
        KeyEvent/VK_X (swap! game-state assoc-in [:team-a :buttons :south] false)
        KeyEvent/VK_C (swap! game-state assoc-in [:team-a :buttons :south-east] false)

        ; team B
        KeyEvent/VK_I (swap! game-state assoc-in [:team-b :buttons :north-west] false)
        KeyEvent/VK_O (swap! game-state assoc-in [:team-b :buttons :north] false)
        KeyEvent/VK_P (swap! game-state assoc-in [:team-b :buttons :north-east] false)
        KeyEvent/VK_K (swap! game-state assoc-in [:team-b :buttons :west] false)
        KeyEvent/VK_SEMICOLON (swap! game-state assoc-in [:team-b :buttons :east] false)
        KeyEvent/VK_COMMA (swap! game-state assoc-in [:team-b :buttons :south-west] false)
        KeyEvent/VK_PERIOD (swap! game-state assoc-in [:team-b :buttons :south] false)
        KeyEvent/VK_SLASH (swap! game-state assoc-in [:team-b :buttons :south-east] false)

        nil)

      (update-game-state))))

;; Mode

(defn mode []
  (reify UiMode

    (init [_]
      (s/reset-pov-offset)
      (stage/set-stage! initial-stage))

    (draw [_]
      ; clear
      (p/background 0)

      (d/pov-view #(mode/draw @stage/game-stage)))

    (key-pressed [_ evt] (mode/key-pressed @stage/game-stage evt))
    (key-released [_ evt] (mode/key-released @stage/game-stage evt))

    (ddr-button-pressed [_ evt] (mode/ddr-button-pressed @stage/game-stage evt))))
