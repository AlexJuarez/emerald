(ns emerald.models.enums
  (:refer-clojure :exclude [update get])
  (:require [emerald.db.core :refer [db]]
            [schema.core :as schema]
            [schema.utils :as utils]
            [schema.macros :as macros]
            [ring.swagger.json-schema :as json-schema :include-macros true]
            [korma.db])
  (:use
   [korma.core]
   [taoensso.timbre :only [trace debug info warn error fatal]]))

(defonce device-types-mem (atom [:desktop :tablet :mobile :multidevice]))
(defonce ad-types-mem (atom [:Display :In-Banner :In-Stream (keyword "Rich Media")]))
(defonce expand-anchors-mem (atom [:bottom :bottomleft :bottomright :left :right :top :topleft :topright]))
(defonce expand-directions-mem (atom [:bottom :left :right :top]))
(defonce expand-types-mem (atom [:traditional :custom :pushdown :takeover]))
(defonce play-modes-mem (atom [:auto :click :rollover]))
(defonce window-types-mem (atom [:new :modal :same]))

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
  {:device_type device-types-mem
   :creative_type ad-types-mem
   :expand_anchor expand-anchors-mem
   :expand_direction expand-directions-mem
   :expand_type expand-types-mem
   :play_mode_type play-modes-mem
   :window_type window-types-mem})

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
   ["select enum_range(NULL::mixpo.creative_type) as ad_types"]
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

(defn expand-directions [] (into [] @expand-anchors-mem))

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

(defn init []
  (info "Populating enums locally")
  (try
    (reset! device-types-mem (device-types*))
    (reset! ad-types-mem (ad-types*))
    (reset! expand-anchors-mem (expand-anchors*))
    (reset! expand-directions-mem (expand-directions*))
    (reset! expand-types-mem (expand-types*))
    (reset! play-modes-mem (play-modes*))
    (reset! window-types-mem (window-types*))
    (catch Exception e (error e "Failed to update enums"))
    ))
