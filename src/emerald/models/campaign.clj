(ns emerald.models.campaign
  (:refer-clojure :exclude [update get])
  (:use
   [korma.db :only (transaction)]
   [korma.core]
   [emerald.db.core]))

(defn get-columns []
  (-> (exec-raw
   ["SELECT column_name FROM information_schema.columns WHERE table_name ='campaigns'"]
   :results)
      ))

(defonce columns (map #(-> % :column_name keyword) (get-columns)))

(defn get [id]
  (->
   (select campaigns
           (with accounts
                 (fields [:name :account.name]))
           (where {:id id}))
   first))

(defn prep [campaign]
  (assoc campaign :id (java.util.UUID/randomUUID)))

(defn add! [campaign]
  (insert campaigns
          (values campaign)))

(defn update! [id campaign]
  (update campaigns
          (set-fields campaign)
          (where {:id id}))
  {:success "updated the campaign"})

(defn all []
  (select campaigns
          (fields :name :id :account_id)
          (limit 10)))
