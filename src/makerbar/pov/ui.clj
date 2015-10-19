(ns makerbar.pov.ui
  (:gen-class
    :extends processing.core.PApplet
    :methods [[captureEvent [processing.video.Capture] void]
              [movieEvent [processing.video.Movie] void]])
  (:import [processing.core PApplet])
  (:require [clojure.core.async :as async :refer (<! go-loop)]
            [clojure.tools.cli :as cli]
            [makerbar.pov.console :as console]
            [makerbar.pov.controller.ddr :as ddr]
            [makerbar.pov.game :as game]
            [makerbar.pov.mode :as m]
            [makerbar.pov.rendersphere :as rendersphere]
            [makerbar.pov.state :as s]
            [makerbar.pov.ui.draw :as d]
            [makerbar.pov.ui.processing :as p]))

(defn setup []
  (p/size (p/display-width) (p/display-height))
  (p/frame-rate 16)

  (d/init)

  (m/add-mode :game (game/mode))
  (m/add-mode :console (console/mode))

  (m/->mode :console)

  (m/init @m/mode))

(defn -setup [this] (p/with-applet this (setup)))
;(defn -sketchFullScreen [this] true)
(defn -draw [this] (p/with-applet this (m/draw @m/mode)))

(defn -keyPressed [this event] (p/with-applet this (m/key-pressed @m/mode event)))
(defn -keyReleased [this event] (p/with-applet this (m/key-released @m/mode event)))

(defn -captureEvent [this camera] (.read camera))
(defn -movieEvent [this movie] (.read movie))

(defn -main
  [& args]

  (let [{{:keys [host port mirror ddr-port]} :options}
        (cli/parse-opts args
                        [["-h" "--host HOST" "Host IP address"]
                         ["-p" "--port PORT" "Port number"
                          :default 10000
                          :parse-fn #(Integer/parseInt %)]
                         ["-m" "--mirror" "Mirror console display"]
                         ["-d" "--ddr-port PORT" "DDR controller port"]])]
    (rendersphere/connect {:host host :port port})
    (when mirror (s/set-state! :console-mirror mirror))

    (println "Available serial ports:" (ddr/list-serial-ports))

    (when ddr-port
      (when-let [ddr-ch (ddr/connect ddr-port)]
        (go-loop []
          (when-let [evt (<! ddr-ch)]
            (m/ddr-button-pressed @m/mode evt)
            (recur))))))

  (PApplet/main "makerbar.pov.ui"))
