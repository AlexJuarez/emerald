(ns emerald.models.campaign
  (:refer-clojure :exclude [update get])
  (:use
   [emerald.util.model]
   [korma.db :only (transaction)]
   [korma.core]
   [emerald.db.core]))

(defonce ^:private changeToArray #{:keywords})

;; (defn get-columns []
;;   (-> (exec-raw
;;    ["SELECT column_name FROM information_schema.columns WHERE table_name ='campaigns'"]
;;    :results)
;;       ))

;; (defonce columns (map #(-> % :column_name keyword) (get-columns)))

(defn get [id]
  (->
   (select campaigns
           (with accounts
                 (fields [:name :account.name]))
           (where {:id id}))
   first))

(defn prep-for-update [account]
  (into {} (map #(update-fields % changeToArray) account)))

(defn prep [campaign]
  (assoc campaign :id (java.util.UUID/randomUUID)))

(defn add! [campaign]
  (insert campaigns
          (values (prep campaign))))

(defn get-pin [campaign-id user-id]
  (first (select campaign-pins
                 (where {:campaign_id campaign-id :user_id user-id}))))

(defn pin! [campaign-id user-id]
  (when (nil? (get-pin campaign-id user-id))
    (insert campaign-pins
            (values {:campaign_id campaign-id :user_id user-id}))))

(defn unpin! [campaign-id user-id]
  (delete campaign-pins
          (where {:campaign_id campaign-id :user_id user-id})))

(defn all-pins [user-id]
  (select campaign-pins
          (fields :campaign_id)))

(defn update! [id campaign]
  (update campaigns
          (set-fields (-> campaign prep prep-for-update))
          (where {:id id}))
  {:success "updated the campaign"})

(defn all []
  (select campaigns
          ;;(fields :name :id :account_id)
          (where {:deleted false})
          (limit 10)))
