(ns emerald.models.placement
  (:refer-clojure :exclude [get update])
  (:require
   [emerald.util.enums :as enum-util])
  (:use
   [emerald.models.helpers]
   [emerald.util.model]
   [korma.db :only (transaction)]
   [korma.core]
   [emerald.db.core]))

(defonce ^:private trackers #{:clickTrackers :impressionTrackers :viewTrackers :keywords})

(defn get [id]
  (->
   (select placements
           (with publishers
                 (fields [:name :publisher.name] [:id :publisher.id]))
           (with targets
                 (where {:type (enum-util/string->enum-sql "creative")})
                 (with creatives
                       (fields :id :name :thumbnail_url_prefix)))
           (where {:id id :deleted false}))
   first
   ))

(defn prep [placement]
  (assoc placement :id (java.util.UUID/randomUUID))
  (assoc placement :expandable true))

(defn prep-for-update [placement]
  (into {} (map #(update-fields % trackers) placement)))

(defn add! [placement]
  (insert placements
          (values (-> placement
                      prep-for-update
                      prep))))

(defn update! [id placement]
  (update placements
          (set-fields (prep-for-update placement))
          (where {:id id}))
  (get id))

(defn children [campaign-ids]
  (let [placement-ids (select-ids placements :campaign_id campaign-ids)]
    {:placement_ids placement-ids}))

(defn all
  ([]
   (all 10 0))
  ([lim os]
   (select placements
           (limit lim)
           (where {:deleted false})
           (offset os))))

(defn all-for-campaign [campaign-id]
   (select placements
           (where {:campaign_id campaign-id})))
