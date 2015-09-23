(ns makerbar.pov.console.serial
  (:require [serial.core :as serial]))

(defn read-byte
  ([in-stream] (read-byte in-stream nil))
  ([in-stream expected-value]
   (let [data (.read in-stream)]
     (when (not= data 0xFF)
       (if expected-value
         (= data expected-value)
         data)))))

(defn ddr-handler [in-stream]
  (when (read-byte in-stream 0xAA)
    (when-let [pad-id (read-byte in-stream)]
      (when-let [cardinal-buttons (read-byte in-stream)]
        (when-let [diagonal-buttons (read-byte in-stream)]
          (prn "pad:" pad-id "cardinal:" cardinal-buttons "diagonal:" diagonal-buttons)
          (read-byte in-stream 0xFF))))))

(defn init-serial []
  (if-let [port-id (first (filter #(.startsWith % "tty.usbserial-")
                                  (map #(.getName %)
                                       (serial/port-identifiers))))]
    (let [port (serial/open port-id)]
      (println "Connected to serial port" port-id)
      (serial/listen! port ddr-handler)

      (.addShutdownHook (Runtime/getRuntime)
                        (Thread. #(do
                                   (println "Closing serial port" port-id)
                                   (serial/close port)))))
    (println "DDR controllers not detected")))
