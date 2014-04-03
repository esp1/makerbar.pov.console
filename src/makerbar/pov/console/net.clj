(ns makerbar.pov.console.net
  (:require [aleph.tcp :as tcp :refer (tcp-client)]
            [byte-streams :as b]
            [gloss.core :as g :refer (defcodec header)]
            [gloss.io :as io :refer (decode encode)]
            [lamina.core :as l]))

(defn get-ch
  [host]
  (l/wait-for-result (tcp-client {:host host, :port 10000})))

(defcodec send-data-codec
  (header #_frame :byte  ; \0
          #_header->body (fn [h] (g/repeated :uint32))
          #_body->header (fn [b] \0)))

(defcodec set-x-offset-codec
  (header #_frame :byte  ; \x
          #_header->body (fn [h] (g/compile-frame :uint32))
          #_body->header (fn [b] \x)))

(defcodec set-brightness-codec
  (header #_frame :byte  ; \b
          #_header->body (fn [h] (g/compile-frame :float32))
          #_body->header (fn [b] \b)))

(defcodec set-contrast-codec
  (header #_frame :byte  ; \c
          #_header->body (fn [h] (g/compile-frame :float32)) 
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

(def ^:private ^:const iarray (class (clojure.core/int-array 0)))

(defn pov-send-data
  [host data]
  (if data
    (let [ch (get-ch host)]
      (l/enqueue ch (encode send-data-codec (seq data))))))
;    (decode return-stats-codec (.toByteBuffer (wait-for-message ch)))))

(defn pov-get-stats
  [host]
  (let [ch (get-ch host)]
    (l/enqueue ch (encode command-codec \?))
    (io/decode return-stats-codec (.toByteBuffer (l/wait-for-message ch)))))

(defn pov-set-x-offset
  [host value]
  (let [ch (get-ch host)]
    (l/enqueue ch (encode set-x-offset-codec value))))
;    (:value (decode return-int-codec (.toByteBuffer (wait-for-message ch))))))

(defn pov-set-brightness
  [host value]
  (let [ch (get-ch host)]
    (l/enqueue ch (encode set-brightness-codec value))))
;    (:value (decode return-float-codec (.toByteBuffer (wait-for-message ch))))))

(defn pov-set-contrast
  [host value]
  (let [ch (get-ch host)]
    (l/enqueue ch (encode set-contrast-codec value))))
;    (:value (decode return-float-codec (.toByteBuffer (wait-for-message ch))))))
