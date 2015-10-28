(ns emerald.models.transaction
  (:refer-clojure :exclude [update get])
  (:require
   [taoensso.timbre :as log]
   [cheshire.core :as jr]
   [emerald.util.session :as session]
   [emerald.cache :as cache])
  (:use
   [emerald.models.helpers]
   [korma.db :only (transaction)]
   [korma.core]
   [emerald.db.core]))

(defn txid [id]
  (let [id (if (instance? java.util.UUID id) (.toString id) id)]
    (str "tx:" id)))

(defn get [id]
  (cache/get (txid id)))

(defn add! [id slug]
  (cache/set (txid id) slug))

(defn delete! [id]
  (cache/delete (txid id)));;TODO make this delete a soft delete

(defn exists? [id]
  (not (empty? (get id))))

(defn remove! [{:keys [client_ids division_ids account_ids campaign_ids placement_ids creative_ids] :as id-map}]
  (let [txid (java.util.UUID/randomUUID)
        results {:tx_id txid
                 :clients (count client_ids)
                 :divisions (count division_ids)
                 :accounts (count account_ids)
                 :campaigns (count campaign_ids)
                 :placements (count placement_ids)
                 :creatives (count creative_ids)}]
    (add! txid id-map)
    (transaction
     (soft-delete-ids clients client_ids)
     (soft-delete-ids divisions division_ids)
     (soft-delete-ids accounts account_ids)
     (soft-delete-ids campaigns campaign_ids)
     (soft-delete-ids placements placement_ids)
     (soft-delete-ids creatives creative_ids))
    (log/info "user_id:" (session/get :user_id) "cascading deletion" (jr/generate-string results))
    results))

(defn restore! [id]
  (let [restore (get id)]
    (when-not (empty? restore)
      (let [{:keys [client_ids division_ids account_ids campaign_ids placement_ids creative_ids]} restore
            results {:clients (count client_ids)
                     :divisions (count division_ids)
                     :accounts (count account_ids)
                     :campaigns (count campaign_ids)
                     :placements (count placement_ids)
                     :creatives (count creative_ids)}]
        (transaction
         (restore-ids clients client_ids)
         (restore-ids divisions division_ids)
         (restore-ids accounts account_ids)
         (restore-ids campaigns campaign_ids)
         (restore-ids placements placement_ids)
         (restore-ids creatives creative_ids))
        (delete! txid)
        (log/info "user_id:" (session/get :user_id) "rolled back transaction" (txid id) (jr/generate-string results))
        results))))
