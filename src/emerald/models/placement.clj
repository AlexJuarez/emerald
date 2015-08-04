(ns emerald.models.placement
  (:refer-clojure :exclude [get update])
  (:use
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
           (with creatives
                 (fields :id :name))
           (where {:id id :deleted false}))
   first
   ))

(defn prep [placement]
  (assoc placement :id (java.util.UUID/randomUUID)))

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
  {:success "updated the placement"})

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
