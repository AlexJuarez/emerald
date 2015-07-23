(ns emerald.models.enums
  (:refer-clojure :exclude [update get])
  (:use
   [korma.core]
   [emerald.db.core]))

(defn device-types []
  (-> (exec-raw
   ["select enum_range(NULL::narwhal.device_type) as device_types"]
   :results)
      first
      (:device_types)
      ))

(defn ad-types []
  (-> (exec-raw
   ["select enum_range(NULL::narwhal.creative_type) as ad_types"]
   :results)
      first
      (:ad_types)
      ))

(defn expand-anchors []
  (-> (exec-raw
   ["select enum_range(NULL::narwhal.expand_anchor) as expand_anchors"]
   :results)
      first
      (:expand_anchors)
      ))

(defn expand-directions []
  (-> (exec-raw
   ["select enum_range(NULL::narwhal.expand_direction) as expand_directions"]
   :results)
      first
      (:expand_directions)
      ))

(defn expand-types []
  (-> (exec-raw
   ["select enum_range(NULL::narwhal.expand_type) as expand_types"]
   :results)
      first
      (:expand_types)
      ))

(defn play-modes []
    (-> (exec-raw
   ["select enum_range(NULL::narwhal.play_mode_type) as play_modes"]
   :results)
      first
      (:play_modes)
      ))

(defn window-types []
    (-> (exec-raw
   ["select enum_range(NULL::narwhal.window_type) as window_types"]
   :results)
      first
      (:window_types)
      ))
