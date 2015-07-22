(ns emerald.models.client
  (:refer-clojure :exclude [get update])
  (:use
   [korma.db :only (transaction)]
   [korma.core]
   [emerald.db.core]))

(defn get [id]
  (->
   (select clients
           (with channels
                 (fields [:name :channel.name] [:type :channel.type]))
           (where {:id id}))
   first))

(defn prep [client]
  (assoc client :id (java.util.UUID/randomUUID)))

(defn add! [client]
  (insert clients
          (values client)))

(defn update! [id client]
  (update clients
          (set-fields client)
          (where {:id id}))
  {:success "updated the client"})

(defn all
  ([]
   (all 10 0))
  ([lim os]
   (select clients
           (limit lim)
           (offset os))))
