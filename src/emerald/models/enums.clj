(ns emerald.models.enums
  (:refer-clojure :exclude [update get])
  (:require [emerald.db.core :refer [db]]
            [korma.db])
  (:use
   [korma.core]))

(defn- device-types* []
  (-> (exec-raw
   ["select enum_range(NULL::mixpo.device_type) as device_types"]
   :results)
      first
      :device_types
      ))

(defonce device-types (device-types*))

(defn- ad-types* []
  (-> (exec-raw
   ["select enum_range(NULL::mixpo.creative_type) as ad_types"]
   :results)
      first
      :ad_types
      ))

(defonce ad-types (ad-types*))

(defn- expand-anchors* []
  (-> (exec-raw
   ["select enum_range(NULL::mixpo.expand_anchor) as expand_anchors"]
   :results)
      first
      :expand_anchors
      ))

(defonce expand-anchors (expand-anchors*))

(defn- expand-directions* []
  (-> (exec-raw
   ["select enum_range(NULL::mixpo.expand_direction) as expand_directions"]
   :results)
      first
      :expand_directions
      ))

(defonce expand-directions (expand-directions*))

(defn- expand-types* []
  (-> (exec-raw
   ["select enum_range(NULL::mixpo.expand_type) as expand_types"]
   :results)
      first
      :expand_types
      ))

(defonce expand-types (expand-types*))

(defn- play-modes* []
    (-> (exec-raw
   ["select enum_range(NULL::mixpo.play_mode_type) as play_modes"]
   :results)
      first
      :play_modes
      ))

(defonce play-modes (play-modes*))

(defn- window-types* []
    (-> (exec-raw
   ["select enum_range(NULL::mixpo.window_type) as window_types"]
   :results)
      first
      :window_types
      ))

(defonce window-types (window-types*))
