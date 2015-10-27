(ns emerald.models.creative
  (:refer-clojure :exclude [update get])
  (:require
   [emerald.util.enums :as enum-util])
  (:use
   [emerald.models.helpers]
   [emerald.util.model]
   [korma.core]
   [emerald.db.core]))

(defonce ^:private changeToArray #{:keywords})

(defn get [id]
  (->
   (select creatives
           (where {:id id}))
   first))

(defn exists? [id]
  (not (empty? (select creatives
                       (where {:id id :deleted false})))))

(defn unique?
  ([m]
    (let [{:keys [campaignId name]} m]
      (empty? (select creatives
                      (where {:campaign_id campaignId :name name})))))
  ([m id]
    (let [creative (get id)
          fields (merge creative m)]
      (unique? fields))))


(defn prep [creative]
  (assoc creative
    :id (java.util.UUID/randomUUID)
    :expandable (= true (not (nil? (:expandMode creative))))))

(defn prep-for-update [creative]
  (into {} (map #(update-fields % changeToArray) creative)))

(defn add! [creative]
  (-> (insert* creatives)
      (values (-> creative prep-for-update prep))
      (exec)))

(defn update! [id creative]
  (update creatives
          (set-fields (-> creative prep-for-update))
          (where {:id id}))
  (get id))

(defn select-child-ids [placement-ids]
 (if (empty? placement-ids)
   []
   (->> (select targets
                (fields :creative.id)
                (join creatives)
                (where {:type (enum-util/string->enum-sql "creative")
                        :placement_id [in placement-ids]}))
        (map #(:id %))
        (into []))))

(defn children [placement-ids]
  (let [creative-ids (select-child-ids placement-ids)]
    {:creative_ids creative-ids}))

(defn all
  ([]
   (all 10 0))
  ([lim os]
   (select creatives
           (where {:deleted false})
           (limit lim)
           (offset os))))

(defn all-for-campaign
  ([campaign-id]
   (select creatives
           (where {:campaign_id campaign-id}))))
