(ns makerbar.pov.console.net
  (:require [aleph.tcp :refer (tcp-client)]
            [gloss.core :refer (compile-frame defcodec enum finite-frame header repeated)]
            [gloss.io :refer (decode encode)]
            [lamina.core :refer (enqueue wait-for-message wait-for-result)]))

(defn get-ch
  [host]
  (wait-for-result (tcp-client {:host host, :port 10000})))

(defcodec send-data-codec
  (header #_frame :byte  ; \0
          #_header->body (fn [h] (finite-frame :unit32 (repeated :uint32 :prefix :none)))
          #_body->header (fn [b] \0)))

(defcodec set-x-offset-codec
  (header #_frame :byte  ; \x
          #_header->body (fn [h] (compile-frame :uint32))
          #_body->header (fn [b] \x)))

(defcodec set-brightness-codec
  (header #_frame :byte  ; \b
          #_header->body (fn [h] (compile-frame :float32))
          #_body->header (fn [b] \b)))

(defcodec set-contrast-codec
  (header #_frame :byte  ; \c
          #_header->body (fn [h] (compile-frame :float32)) 
          #_body->header (fn [b] \c)))

(defcodec command-codec
  :byte)

(defcodec return-stats-codec
  {:rps :float64-le
   :fps :float64-le})

(defcodec return-int-codec
  {:value :uint32-le})

(defcodec return-float-codec
  {:value :float32-le})

(defn pov-send-data
  [host data]
;    (println "host" host ", port" port ", data" (count data))
  (let [ch (get-ch host)]
    (enqueue ch (encode send-data-codec data))))
;    (decode return-stats-codec (.toByteBuffer (wait-for-message ch)))))

(defn pov-get-stats
  [host]
  (let [ch (get-ch host)]
    (enqueue ch (encode command-codec \?))
    (decode return-stats-codec (.toByteBuffer (wait-for-message ch)))))

(defn pov-set-x-offset
  [host value]
  (let [ch (get-ch host)]
    (enqueue ch (encode set-x-offset-codec value))))
;    (:value (decode return-int-codec (.toByteBuffer (wait-for-message ch))))))

(defn pov-set-brightness
  [host value]
  (let [ch (get-ch host)]
    (enqueue ch (encode set-brightness-codec value))))
;    (:value (decode return-float-codec (.toByteBuffer (wait-for-message ch))))))

(defn pov-set-contrast
  [host value]
  (let [ch (get-ch host)]
    (enqueue ch (encode set-contrast-codec value))))
;    (:value (decode return-float-codec (.toByteBuffer (wait-for-message ch))))))
