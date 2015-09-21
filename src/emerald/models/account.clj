(ns emerald.models.account
  (:refer-clojure :exclude [update get])
  (:use
   [emerald.util.model]
   [korma.db :only (transaction)]
   [korma.core]
   [emerald.db.core]))

(defonce ^:private changeToArray #{:keywords})

(defn exists? [id]
  (-> (select accounts
              (where {:id id}))
      empty?
      not
      ))

(defn access? [id user-id]
  (->
   (select user-account-permissions
           (where {:account_id id :user_id user-id}))
   empty?
   not))

(defn prep-for-update [account]
  (->
   (into {} (map #(update-fields % changeToArray) account))
   (dissoc :pinned)))

(defn prep [account]
  (assoc account :id (java.util.UUID/randomUUID)))

(defn get-pin [account-id user-id]
  (first (select account-pins
                 (where {:account_id account-id :user_id user-id}))))

(defn pinned? [id user-id]
  (not (nil? (get-pin id user-id))))

(defn get
  ([id] (get id nil))
  ([id user-id]
   (->
    (select accounts
            (with industries
                  (fields [:name :industry.name]))
            (where {:id id}))
    first
    (assoc :pinned (pinned? id user-id)))))

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

(defn update! [id account user-id]
  (transaction
   (let [account (prep-for-update account)]
     (when (not (empty? account))
       (update accounts
               (set-fields (-> account prep-for-update))
               (where {:id id}))))
   (if (:pinned account) (pin! id user-id))
   (if (= false (:pinned account)) (unpin! id user-id))
   )
  (get id user-id))

(defn all
  ([]
   (all 10 0))
  ([lim os]
   (select accounts
           (where {:deleted false})
           (limit lim)
           (offset os))))
