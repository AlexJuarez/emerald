(ns emerald.models.enums
  (:refer-clojure :exclude [update get])
  (:use
   [korma.core]
   [emerald.db.core]))

(defn- device-types* []
  (-> (exec-raw
   ["select enum_range(NULL::narwhal.device_type) as device_types"]
   :results)
      first
      (:device_types)
      ))

(defonce device-types (atom (map keyword (device-types*))))

(defn- ad-types* []
  (-> (exec-raw
   ["select enum_range(NULL::narwhal.creative_type) as ad_types"]
   :results)
      first
      (:ad_types)
      ))

(defonce ad-types (atom (map keyword (ad-types*))))

(defn- expand-anchors* []
  (-> (exec-raw
   ["select enum_range(NULL::narwhal.expand_anchor) as expand_anchors"]
   :results)
      first
      (:expand_anchors)
      ))

(defonce expand-anchors (atom (map keyword (expand-anchors*))))

(defn- expand-directions* []
  (-> (exec-raw
   ["select enum_range(NULL::narwhal.expand_direction) as expand_directions"]
   :results)
      first
      (:expand_directions)
      ))

(defonce expand-directions (atom (map keyword (expand-directions*))))

(defn- expand-types* []
  (-> (exec-raw
   ["select enum_range(NULL::narwhal.expand_type) as expand_types"]
   :results)
      first
      (:expand_types)
      ))

(defonce expand-types (atom (map keyword (expand-types*))))

(defn- play-modes* []
    (-> (exec-raw
   ["select enum_range(NULL::narwhal.play_mode_type) as play_modes"]
   :results)
      first
      (:play_modes)
      ))

(defonce play-modes (atom (map keyword (play-modes*))))

(defn- window-types* []
    (-> (exec-raw
   ["select enum_range(NULL::narwhal.window_type) as window_types"]
   :results)
      first
      (:window_types)
      ))

(defonce window-types (atom (map keyword (window-types*))))
