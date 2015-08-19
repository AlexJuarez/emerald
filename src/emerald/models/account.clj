(ns emerald.models.account
  (:refer-clojure :exclude [update get])
  (:use
   [emerald.util.model]
   [korma.db :only (transaction)]
   [korma.core]
   [emerald.db.core]))

(defonce ^:private changeToArray #{:keywords})

(defn get [id]
  (->
   (select accounts
           (with industries
                 (fields [:name :industry.name]))
           (where {:id id}))
   first))

(defn exists? [id]
  (not (empty? (select accounts
                       (where {:id id})))))

(defn access? [id user-id]
  (->
   (select user-account-permissions
           (where {:account_id id :user_id user-id}))
   empty?
   not))

(defn prep-for-update [account]
  (into {} (map #(update-fields % changeToArray) account)))

(defn prep [account]
  (assoc account :id (java.util.UUID/randomUUID)))

(defn get-pin [account-id user-id]
  (first (select account-pins
                 (where {:account_id account-id :user_id user-id}))))

(defn pin! [account-id user-id]
  (when (nil? (get-pin account-id user-id))
    (insert account-pins
            (values {:account_id account-id :user_id user-id}))))

(defn unpin! [account-id user-id]
  (delete account-pins
          (where {:account_id account-id :user_id user-id})))

(defn all-pins [user-id]
  (select account-pins
          (fields :account_id)))

(defn add! [account]
  (insert accounts
          (values (-> account prep prep-for-update))))

(defn update! [id account]
  (update accounts
          (set-fields (-> account prep-for-update))
          (where {:id id}))
  {:success "updated the account"})

(defn all
  ([]
   (all 10 0))
  ([lim os]
   (select accounts
           (where {:deleted false})
           (limit lim)
           (offset os))))
