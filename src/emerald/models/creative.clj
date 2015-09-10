(ns emerald.models.creative
  (:refer-clojure :exclude [update get])
  (:use
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
  {:success "updated the creative"})

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
