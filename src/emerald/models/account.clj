(ns emerald.models.account
  (:refer-clojure :exclude [update get])
  (:use
   [korma.db :only (transaction)]
   [korma.core]
   [emerald.db.core]))

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

(defn prep [account]
  (assoc account :id (java.util.UUID/randomUUID)))

(defn add! [account]
  (insert accounts
          (values account)))

(defn update! [id account]
  (update accounts
          (set-fields account)
          (where {:id id}))
  {:success "updated the account"})

(defn all
  ([]
   (all 10 0))
  ([lim os]
   (select accounts
           (limit lim)
           (offset os))))
