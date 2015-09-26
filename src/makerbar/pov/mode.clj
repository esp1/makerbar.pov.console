(ns makerbar.pov.mode)

(def mode (atom nil))

(defn set-mode! [m]
  (reset! mode m))

(defprotocol UiMode
  (draw [_])
  (key-pressed [_ event]))