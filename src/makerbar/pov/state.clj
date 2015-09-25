(ns makerbar.pov.state)


; Constants

(def pov-width (* 4 56))
(def pov-height (* 6 17))

(def default-state
  {:brightness 0
   :contrast 1
   
   :pov-offset [0 0]
   
   :img-width pov-width
   :img-height pov-height
   :img-offset [0 0]
   :img-scale 1
   
   :rotation-direction 1
   :rotation-speed 0
   :flip-image false})

(def initial-state
  (merge default-state
         {:console-mirror false
          :console-fade 0}))


; State

(def state (atom initial-state))


; Functions

(defn get-state
  ([] @state)
  ([attr] (get @state attr)))

(defn set-state!
  ([attr val] (swap! state assoc attr val))
  ([attr1 val1 attr2 val2] (swap! state assoc attr1 val1 attr2 val2)))

(defn update-state!
  [attr f]
  (swap! state update-in [attr] f))

(defn wrapped
  ([value range]
    (let [mval (mod value range)]
      (if (< mval 0) (+ mval range)
        (if (< range mval) (- mval range)
          mval)))))

(defn inc-pov-offset
  [[dx dy]]
  (swap! state update-in
         [:pov-offset]
         (fn [[x y]]
           [(wrapped (+ x dx) pov-width) (wrapped (+ y dy) pov-height)])))

(defn inc-img-scale [ds] (swap! state update-in [:img-scale] #(+ % (* 0.01 ds))))

(defn inc-img-offset [offset] (swap! state update-in [:img-offset] #(map + % offset)))

(defn rotation-speed [speed] (swap! state assoc :rotation-speed speed))
(defn toggle-rotation-direction [] (swap! state assoc :rotation-direction (- (:rotation-direction @state))))

(defn toggle-flip-image []
  (swap! state assoc :flip-image (not (:flip-image @state)))
  (toggle-rotation-direction))

(defn inc-brightness [db] (swap! state update-in [:brightness] #(+ % db)))
(defn inc-contrast [dc] (swap! state update-in [:contrast] #(max 1 (+ % dc))))

(defn reset-settings [] (reset! state (merge @state default-state)))

(defn display-status []
  (apply str (map (fn [[key val]]
                    (str (name key) ": " val \newline))
                  @state)))
