(ns emerald.models.enums
  (:refer-clojure :exclude [update get])
  (:require [emerald.db.core :refer [db]]
            [korma.db])
  (:use
   [korma.core]))

(defn- device-types* []
  (-> (exec-raw
   ["select enum_range(NULL::narwhal.device_type) as device_types"]
   :results)
      first
      :device_types
      ))

(defonce device-types (atom []))

(defn- ad-types* []
  (-> (exec-raw
   ["select enum_range(NULL::narwhal.creative_type) as ad_types"]
   :results)
      first
      :ad_types
      ))

(defonce ad-types (atom []))

(defn- expand-anchors* []
  (-> (exec-raw
   ["select enum_range(NULL::narwhal.expand_anchor) as expand_anchors"]
   :results)
      first
      :expand_anchors
      ))

(defonce expand-anchors (atom []))

(defn- expand-directions* []
  (-> (exec-raw
   ["select enum_range(NULL::narwhal.expand_direction) as expand_directions"]
   :results)
      first
      :expand_directions
      ))

(defonce expand-directions (atom []))

(defn- expand-types* []
  (-> (exec-raw
   ["select enum_range(NULL::narwhal.expand_type) as expand_types"]
   :results)
      first
      :expand_types
      ))

(defonce expand-types (atom []))

(defn- play-modes* []
    (-> (exec-raw
   ["select enum_range(NULL::narwhal.play_mode_type) as play_modes"]
   :results)
      first
      :play_modes
      ))

(defonce play-modes (atom []))

(defn- window-types* []
    (-> (exec-raw
   ["select enum_range(NULL::narwhal.window_type) as window_types"]
   :results)
      first
      :window_types
      ))

(defonce window-types (atom []))

(defn init []
  (reset! device-types (map keyword (device-types*)))
  (reset! ad-types (map keyword (ad-types*)))
  (reset! expand-anchors (map keyword (expand-anchors*)))
  (reset! expand-directions (map keyword (expand-directions*)))
  (reset! expand-types (map keyword (expand-types*)))
  (reset! play-modes (map keyword (play-modes*)))
  (reset! window-types (map keyword (window-types*))))
