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

(defn get-pin [client-id user-id]
  (first (select client-pins
                 (where {:client_id client-id :user_id user-id}))))

(defn pin! [client-id user-id]
  (when (nil? (get-pin client-id user-id))
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
           (where {:deleted false})
           (limit lim)
           (offset os))))
