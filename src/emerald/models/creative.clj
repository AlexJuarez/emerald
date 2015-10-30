(ns emerald.models.creative
  (:refer-clojure :exclude [update get])
  (:require
   [emerald.util.enums :as enum-util])
  (:use
   [emerald.models.helpers]
   [emerald.util.model]
   [korma.core]
   [korma.db :only (transaction)]
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
    :expandable (= true (not (nil? (:expandType creative))))))

(defn prep-for-update [creative]
  (into {} (map #(update-fields % changeToArray) creative)))

(defn add-display! [creative]
  ; define 2 helper functions for populating media-item based and server-generated, to combine
  ; with the user-input creatvie map
  (letfn [(add-display-timeline-templates [creative]
            (into creative 
              { :videotimeline (raw
                  (str "'<visualtimeline><slide duration=\"0\" guid=\"" 
                       (:mediaId creative) 
                       "\"/></visualtimeline>'::XML"))
                :audiotimeline (raw "'<audiotimeline/>'::XML")
                :texttimeline  (raw "'<texttimeline/>'::XML") }))

          (add-media-info [creative]
            (-> creative
              (into 
                (select media 
                  (fields [:width :embed_width] [:height :embed_height] [:url_prefix :thumbnail_url_prefix])
                  (where { :id (:mediaId creative) })))
              (dissoc :mediaId)))]

    (transaction
        (let [creative-inserted
          (-> (insert* creatives)
              (values (-> creative 
                        prep-for-update 
                        prep 
                        add-display-timeline-templates
                        add-media-info))
              (exec))]
          (insert creative-media 
            (values { :creative_id (:id creative-inserted) 
                      :media_id (:mediaId creative) }))
          creative-inserted))))

(defn add! [creative]
  (if
    (= :Display (:value (:type creative)))
      (add-display! creative)

      ; default to temporary 'fake' creative insert, no real ad content
      (-> (insert* creatives)
        (values (-> creative prep-for-update prep))
        (exec))))

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
