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

(defn exists? [id]
  (->
   (select campaigns
           (where {:id id}))
   empty?
   not))

(defn prep-for-update [campaign]
  (-> (into {} (map #(update-fields % changeToArray) campaign))
      (dissoc :pinned)))


(defn prep [campaign]
  (assoc campaign :id (java.util.UUID/randomUUID)))

(defn add! [campaign]
  (insert campaigns
          (values (-> campaign prep prep-for-update))))

(defn get-pin [campaign-id user-id]
  (first (select campaign-pins
                 (where {:campaign_id campaign-id :user_id user-id}))))

(defn pinned? [id user-id]
  (not (nil? (get-pin id user-id))))

(defn get
  ([id] (get id nil))
  ([id user-id]
   (->
    (select campaigns
            (with accounts
                  (fields [:name :account.name]))
            (where {:id id}))
    first
    (assoc :pinned (pinned? id user-id)))))

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

(defn update! [id campaign user-id]
  (transaction
   (let [campaign (prep-for-update campaign)]
     (when (not (empty? campaign))
       (update campaigns
               (set-fields (-> campaign prep-for-update))
               (where {:id id}))))
   (if (:pinned campaign) (pin! id user-id))
   (if (= false (:pinned campaign)) (unpin! id user-id)))
  (get id user-id))

(defn all
  ([]
   (all 10 0))
  ([lim os]
    (select campaigns
            ;;(fields :name :id :account_id)
            (where {:deleted false})
            (limit lim)
            (offset os))))
