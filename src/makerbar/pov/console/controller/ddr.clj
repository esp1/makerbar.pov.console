(ns makerbar.pov.console.controller.ddr
  (:require [makerbar.pov.console.state :as s]
            [serial.core :as serial]
            [clojure.core.async :as async]))

(def button-nw 0x0200)                                      ; x
(def button-n 0x0008)                                       ; up
(def button-ne 0x0400)                                      ; o

(def button-w 0x0001)                                       ; left
(def button-e 0x0004)                                       ; right

(def button-sw 0x0800)                                      ; triangle
(def button-s 0x0002)                                       ; down
(def button-se 0x0100)                                      ; square


(defn read-byte
  ([in-stream] (read-byte in-stream nil))
  ([in-stream expected-value]
   (let [data (.read in-stream)]
     (when (not= data 0xFF)
       (if expected-value
         (= data expected-value)
         data)))))

(defn ddr-serial [ch #_core.async.channel]
  (fn [in-stream]
    (when (read-byte in-stream 0xAA)
      (when-let [controller-id (read-byte in-stream)]
        (when-let [buttons0 (read-byte in-stream)]
          (when-let [buttons1 (read-byte in-stream)]
            (let [buttons (+ (bit-shift-left buttons1 8) buttons0)]
              (async/put! ch {:controller-id (condp = controller-id
                                               1 :controller-A
                                               2 :controller-B)
                              :buttons       {:north      (not= 0 (bit-and buttons button-n))
                                              :south      (not= 0 (bit-and buttons button-s))
                                              :east       (not= 0 (bit-and buttons button-e))
                                              :west       (not= 0 (bit-and buttons button-w))
                                              :north-west (not= 0 (bit-and buttons button-nw))
                                              :north-east (not= 0 (bit-and buttons button-ne))
                                              :south-west (not= 0 (bit-and buttons button-sw))
                                              :south-east (not= 0 (bit-and buttons button-se))}})
              (read-byte in-stream 0xFF))))))))

(defn init-ddr
  "Connect to DDR controllers. Returns a core.async channel where controller events will be sent to."
  []
  (if-let [port-id (first (filter #(.startsWith % "tty.usbserial-")
                                  (map #(.getName %)
                                       (serial/port-identifiers))))]
    (let [port (serial/open port-id)
          ch (async/chan (async/sliding-buffer 10))]
      (println "Connected to DDR serial port" port-id)
      (serial/listen! port (ddr-serial ch))

      (.addShutdownHook (Runtime/getRuntime)
                        (Thread. #(do
                                   (println "Closing DDR serial port" port-id)
                                   (serial/close port)
                                   (async/close! ch))))
      ch)
    (println "DDR controllers not detected")))
