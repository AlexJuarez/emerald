(ns emerald.models.client
  (:refer-clojure :exclude [get update])
  (:require
   [emerald.cache :as cache]
   [emerald.util.enums :as enum-util])
  (:use
   [emerald.models.helpers]
   [korma.db :only (transaction)]
   [korma.core]
   [emerald.db.core]))

(defn unique-name? [name]
  (->
   (select clients
           (where {:name name}))
   empty?))

(defn prep [client]
  (assoc client :id (java.util.UUID/randomUUID)))

(defn prep-for-update [client]
  (dissoc client :pinned))

(defn get-pin [client-id user-id]
  (first (select client-pins
                 (where {:client_id client-id :user_id user-id}))))

(defn pinned? [id user-id]
  (not (nil? (get-pin id user-id))))

(defn get
  ([id user-id]
    (->
     (select clients
             (with channels
                   (fields [:name :channel.name] [:type :channel.type]))
             (where {:id id}))
     first
     (assoc :pinned (pinned? id user-id))))
  ([id]
   (get id nil)))

(defn exists? [id]
  (not (empty? (get id))))

(defn pin! [client-id user-id]
  (when (not (pinned? client-id user-id))
    (insert client-pins
            (values {:client_id client-id :user_id user-id}))))

(defn unpin! [client-id user-id]
  (delete client-pins
          (where {:client_id client-id :user_id user-id})))

(defn all-pins [user-id]
  (select client-pins
          (fields :client_id)))

(defn add! [client]
  (insert clients
          (values (-> client prep prep-for-update))))

(defn children [id]
  (let [client (first (select clients
                              (with divisions
                                    (fields :id))
                              (where {:id id})))
        division-ids (map #(:id %) (:divisions client))
        account-ids (select-ids accounts :division_id division-ids)
        campaign-ids (select-ids campaigns :account_id account-ids)
        placement-ids (select-ids placements :campaign_id campaign-ids)
        targets (select targets
                        (fields :creative.id)
                        (join creatives)
                        (where {:type (enum-util/string->enum-sql "creative")
                                :placement_id [in placement-ids]}))
        creative-ids (->> targets
                          (map #(:id %))
                          (into []))]
    {:division-ids division-ids
     :account-ids account-ids
     :campaign-ids campaign-ids
     :placement-ids placement-ids
     :creative-ids creative-ids}))

(defn remove! [id]
  (let [ids (children id)
        txid (java.util.UUID/randomUUID)]
    (cache/set (str "tx:" (.toString txid)) {:id id :type :client :ids ids})
    (update clients (set-fields {:deleted true}) (where {:id id}))
    (soft-delete-ids divisions (:division-ids ids))
    (soft-delete-ids accounts (:account-ids ids))
    (soft-delete-ids campaigns (:campaign-ids ids))
    (soft-delete-ids placements (:placement-ids ids))
    (soft-delete-ids creatives (:creative-ids ids))
    {:tx_id txid
     :divisions (count (:division-ids ids))
     :accounts (count (:account-ids ids))
     :campaigns (count (:campaign-ids ids))
     :placements (count (:placement-ids ids))
     :creatives (count (:creative-ids ids))}))

(defn restore! [txid]
  (let [restore (cache/get (str "tx:" txid))
        id (:id restore)]
    (when (and (not (nil? restore))
               (= :client (:type restore)))
      (cache/delete (str "tx:" txid))
      (let [ids (:ids restore)]
        (update clients (set-fields {:deleted false}) (where {:id id}))
        {:divisions (restore-ids divisions (:division-ids ids))
         :accounts (restore-ids accounts (:account-ids ids))
         :campaigns (restore-ids campaigns (:campaign-ids ids))
         :placements (restore-ids placements (:placement-ids ids))
         :creatives (restore-ids creatives (:creative-ids ids))}))))

(defn update! [id client user-id]
  (transaction
   (let [client (prep-for-update client)]
     (when (not (empty? client))
       (update clients
               (set-fields client)
               (where {:id id}))))
   (if (:pinned client) (pin! id user-id))
   (if (= (:pinned client) false) (unpin! id user-id)))
  (get id user-id))

(defn all
  ([]
   (all 10 0))
  ([lim os]
   (select clients
           (where {:deleted false})
           (limit lim)
           (offset os))))
