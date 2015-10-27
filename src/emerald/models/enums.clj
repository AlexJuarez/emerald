(ns emerald.models.enums
  (:refer-clojure :exclude [update get])
  (:require [emerald.db.core :refer [db]]
            [schema.core :as schema]
            [schema.utils :as utils]
            [schema.macros :as macros]
            [ring.swagger.json-schema :as json-schema :include-macros true]
            [taoensso.timbre :as log]
            [korma.db])
  (:use
   [korma.core]))

(defonce device-types-mem (atom [:desktop :tablet :mobile :multidevice]))
(defonce ad-types-mem (atom [:Display :In-Banner :In-Stream (keyword "Rich Media")]))
(defonce expand-anchors-mem (atom [:bottom :bottomleft :bottomright :left :right :top :topleft :topright]))
(defonce expand-directions-mem (atom [:bottom :left :right :top]))
(defonce expand-types-mem (atom [:traditional :directional :pushdown :takeover]))
(defonce play-modes-mem (atom [:auto :click :rollover]))
(defonce window-types-mem (atom [:new :modal :same]))
(defonce target-types-mem (atom [:creative :daypart (keyword "device target") :rotate :sequence (keyword "survey control")]))
(defonce rate-types-mem (atom [:CPC :CPM :CPV :CPCV (keyword "Fixed Fee") (keyword "Added Value")]))
(defonce vast-mime-types-mem (atom [:video/x-mp4 :video/mp4]))
(defonce vast-media-file-types-mem (atom [:universal :flash :javascript]))

(defrecord KormaEnumSchema [vs]
  ;;based on 0.4.4 version of schema
  ;;https://github.com/Prismatic/schema/blob/0ad28bff3ec03130cfa25b16138c6b1adc143011/src/cljx/schema/core.cljx
  ;;changed to spec in v1.0...
  schema.core.Schema
  (walker [this]
        (fn [x]
          (if (contains? vs (:value x))
            x
            (macros/validation-error this x (list vs (utils/value-name (:value x)))))))
  (explain [this] (cons 'enum-type vs)))

(defn enum-type
  "A value that must be = to some element of vs."
  [& vs]
  (KormaEnumSchema. (set vs)))

(extend-type KormaEnumSchema
  json-schema/JsonSchema
  (convert [e {:keys [in]}]
           (merge (json-schema/->swagger (class (first (:vs e)))) {:enum (seq (:vs e))})))

(def enum-types
  {:mixpo.device_type device-types-mem
   :mixpo.ad_type ad-types-mem
   :mixpo.expand_anchor expand-anchors-mem
   :mixpo.expand_direction expand-directions-mem
   :mixpo.expand_type expand-types-mem
   :mixpo.play_mode_type play-modes-mem
   :mixpo.window_type window-types-mem
   :mixpo.target_type target-types-mem
   :mixpo.vast_mime_type vast-mime-types-mem
   :mixpo.vast_media_file_type vast-media-file-types-mem
   :mixpo.rate_type rate-types-mem})

(defn get-enum-type [v]
  (->
   (filter #(some #{v} @(val %)) enum-types)
   first
   key))

(defn convert-keyword [lst]
  (map #(keyword %) lst))

(defn- device-types* []
  (-> (exec-raw
   ["select enum_range(NULL::mixpo.device_type) as device_types"]
   :results)
      first
      :device_types
      convert-keyword
      ))

(defn device-types [] (into [] @device-types-mem))

(defn- ad-types* []
  (-> (exec-raw
   ["select enum_range(NULL::mixpo.ad_type) as ad_types"]
   :results)
      first
      :ad_types
      convert-keyword
      ))

(defn ad-types [] (into [] @ad-types-mem))

(defn- expand-anchors* []
  (-> (exec-raw
   ["select enum_range(NULL::mixpo.expand_anchor) as expand_anchors"]
   :results)
      first
      :expand_anchors
      convert-keyword
      ))

(defn expand-anchors [] (into [] @expand-anchors-mem))

(defn- expand-directions* []
  (-> (exec-raw
   ["select enum_range(NULL::mixpo.expand_direction) as expand_directions"]
   :results)
      first
      :expand_directions
      convert-keyword
      ))

(defn expand-directions [] (into [] @expand-directions-mem))

(defn- expand-types* []
  (-> (exec-raw
   ["select enum_range(NULL::mixpo.expand_type) as expand_types"]
   :results)
      first
      :expand_types
      convert-keyword
      ))

(defn expand-types [] (into [] @expand-types-mem))

(defn- play-modes* []
    (-> (exec-raw
   ["select enum_range(NULL::mixpo.play_mode_type) as play_modes"]
   :results)
      first
      :play_modes
      convert-keyword
      ))

(defn play-modes [] (into [] @play-modes-mem))

(defn- window-types* []
    (-> (exec-raw
   ["select enum_range(NULL::mixpo.window_type) as window_types"]
   :results)
      first
      :window_types
      convert-keyword
      ))

(defn window-types [] (into [] @window-types-mem))

(defn- target-types* []
  (-> (exec-raw
   ["select enum_range(NULL::mixpo.target_type) as target_types"]
   :results)
      first
      :target_types
      convert-keyword
      ))

(defn target-types [] (into [] @target-types-mem))

(defn- rate-types* []
    (-> (exec-raw
   ["select enum_range(NULL::mixpo.rate_type) as rate_types"]
   :results)
      first
      :rate_types
      convert-keyword
      ))

(defn rate-types [] (into [] @rate-types-mem))

(defn- vast-mime-types* []
  (-> (exec-raw
   ["select enum_range(NULL::mixpo.vast_mime_type) as vast_mime_types"]
   :results)
      first
      :vast_mime_types
      convert-keyword
      ))

(defn vast-mime-types [] (into [] @vast-mime-types-mem))

(defn- vast-media-file-types* []
  (-> (exec-raw
   ["select enum_range(NULL::mixpo.vast_media_file_type) as vast_media_file_types"]
   :results)
      first
      :vast_media_file_types
      convert-keyword
      ))

(defn vast-media-file-types [] (into [] @vast-media-file-types-mem))

(defn reset-enum [mem new-value-fn]
  (try
    (let [new-value (new-value-fn)]
      (when-not (= (sort @mem) (sort new-value))
        (log/warn "Enum value has changed" @mem (into [] new-value)))
      (reset! mem new-value))
    (catch Exception e (log/error e "Failed to update one of the enums"))))

(defn init []
  (log/info "Populating enums locally")
  (reset-enum device-types-mem device-types*)
  (reset-enum ad-types-mem ad-types*)
  (reset-enum expand-anchors-mem expand-anchors*)
  (reset-enum expand-directions-mem expand-directions*)
  (reset-enum expand-types-mem expand-types*)
  (reset-enum play-modes-mem play-modes*)
  (reset-enum window-types-mem window-types*)
  (reset-enum target-types-mem target-types*)
  (reset-enum vast-mime-types-mem vast-mime-types*)
  (reset-enum vast-media-file-types-mem vast-media-file-types*)
  (reset-enum rate-types-mem rate-types*))
