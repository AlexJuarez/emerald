(ns emerald.models.placement
  (:refer-clojure :exclude [get update])
  (:use
   [korma.db :only (transaction)]
   [korma.core]
   [emerald.db.core])
  (:require
   [clojure.string :as s]))

(defn camel-case [m]
  (into {}
        (for [[k v] m]
          [(let [t (s/split (name k) #"_")]
             (apply str (first t) (map s/capitalize (rest t)))) v]
     )))

(defn get [id]
  (->
   (select placements
           (fields :name
                   :id
                   :play_mode
                   :skip_321
                   :audio_off
                   :mute_on_roll_out
                   :hotspot
                   :open_links
                   :allow_animations
                   :flight_start
                   :flight_end
                   :booked_impressions
                   ;;keyword
                   :cost ;;interperate with rate_type
                   :rate_type
                   :type
                   :embed_height
                   :embed_width
                   :spanish)
           (with publishers
                 (fields [:name :publisher.name] [:id :publisher.id]))
           (with creatives
                 (fields :id :name))
           (where {:id id}))
   first
   camel-case
   ))

(defn all
  ([]
   (all 10 0))
  ([lim os]
   (select placements
           (limit lim)
           (offset os))))
