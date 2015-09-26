(ns makerbar.pov.mode)

(def mode (atom nil))

(defn set-mode! [m]
  (reset! mode m))

(defprotocol UiMode
  (init [_])
  (draw [_])
  (key-pressed [_ event])
  (key-released [_ event])
  (ddr-button-pressed [_ event]))