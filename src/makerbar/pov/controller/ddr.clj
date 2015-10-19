(ns makerbar.pov.controller.ddr
  (:require [makerbar.pov.state :as s]
            [serial.core :as serial]
            [clojure.core.async :as async]))

(def button-masks
  {:north-west 0x0200                                       ; x
   :north      0x0008                                       ; up
   :north-east 0x0400                                       ; o

   :west       0x0001                                       ; left
   :east       0x0004                                       ; right

   :south-west 0x0800                                       ; triangle
   :south      0x0002                                       ; down
   :south-east 0x100                                        ; square
   })


(defn read-byte
  ([in-stream] (read-byte in-stream nil))
  ([in-stream expected-value]
   (let [data (.read in-stream)]
     (when (not= data 0xFF)
       (if expected-value
         (= data expected-value)
         data)))))

(defn read-button-bytes [in-stream]
  (when-let [cardinal-buttons (read-byte in-stream)]
    (when-let [diagonal-buttons (read-byte in-stream)]
      (+ (bit-shift-left diagonal-buttons 8)
         cardinal-buttons))))

(defn deserialize-button-value [button-bytes]
  (apply merge
         (for [[kw mask] button-masks]
           {kw (not= 0 (bit-and button-bytes mask))})))

(defn ddr-serial [ch #_core.async.channel]
  (fn [in-stream]
    (when (read-byte in-stream 0xAA)
      (when-let [a-button-bytes (read-button-bytes in-stream)]
        (when-let [b-button-bytes (read-button-bytes in-stream)]
          (async/put! ch {:ddr-a (deserialize-button-value a-button-bytes)
                          :ddr-b (deserialize-button-value b-button-bytes)})
          (read-byte in-stream 0xFF))))))

(defn list-serial-ports []
  (map #(.getName %) (serial/port-identifiers)))

(defn connect
  "Connect to DDR controllers. Returns a core.async channel where controller events will be sent to."
  [port-id]
  (println "Connecting to DDR controller at" port-id)
  (let [port (serial/open port-id)
        ch (async/chan (async/sliding-buffer 10))]
    (println "Connected to DDR serial port" port-id)
    (serial/listen! port (ddr-serial ch))

    (.addShutdownHook (Runtime/getRuntime)
                      (Thread. #(do
                                 (println "Closing DDR serial port" port-id)
                                 (serial/close port)
                                 (async/close! ch))))
    ch))

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