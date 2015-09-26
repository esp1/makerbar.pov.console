(ns makerbar.pov.mode)

(def modes (atom {}))

(defn add-mode [kw m]
  (println "Adding mode" kw)
  (swap! modes conj [kw m]))

(def mode (atom nil))

(defprotocol UiMode
  (init [_])
  (draw [_])
  (key-pressed [_ event])
  (key-released [_ event])
  (ddr-button-pressed [_ event]))

(defn ->mode [kw]
  (println "Switching to mode" kw)
  (let [m (get @modes kw)]
    (init m)
    (reset! mode m)))