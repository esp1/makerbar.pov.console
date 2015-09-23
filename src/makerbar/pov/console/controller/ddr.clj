(ns makerbar.pov.console.controller.ddr
  (:require [makerbar.pov.console.state :as s]
            [serial.core :as serial]))

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

;(defprotocol DDRHandler
;  (north [this])
;  (sourth [this])
;  (east [this])
;  (west [this])
;  (north-east [this])
;  (north-west [this])
;  (south-east [this])
;  (south-west [this]))

(defn ddr-serial [in-stream]
  (when (read-byte in-stream 0xAA)
    (when-let [pad-id (read-byte in-stream)]
      (when-let [buttons0 (read-byte in-stream)]
        (when-let [buttons1 (read-byte in-stream)]
          (let [buttons (+ (bit-shift-left buttons1 8) buttons0)]
            (prn "pad:" pad-id "buttons:" buttons "a:" buttons0 "b:" buttons1)
            (when (not= 0 (bit-and buttons button-n)) (s/inc-pov-offset [0 -1]))
            (when (not= 0 (bit-and buttons button-s)) (s/inc-pov-offset [0 1]))
            (when (not= 0 (bit-and buttons button-e)) (s/inc-pov-offset [1 0]))
            (when (not= 0 (bit-and buttons button-w)) (s/inc-pov-offset [-1 0]))
            ;(when (bit-and buttons button-nw) (north ddr-handler))
            ;(when (bit-and buttons button-ne) (north ddr-handler))
            ;(when (bit-and buttons button-sw) (north ddr-handler))
            ;(when (bit-and buttons button-se) (north ddr-handler))
            (read-byte in-stream 0xFF)))))))

(defn init-ddr []
  (if-let [port-id (first (filter #(.startsWith % "tty.usbserial-")
                                  (map #(.getName %)
                                       (serial/port-identifiers))))]
    (let [port (serial/open port-id)]
      (println "Connected to DDR serial port" port-id)
      (serial/listen! port ddr-serial)

      (.addShutdownHook (Runtime/getRuntime)
                        (Thread. #(do
                                   (println "Closing DDR serial port" port-id)
                                   (serial/close port)))))
    (println "DDR controllers not detected")))
