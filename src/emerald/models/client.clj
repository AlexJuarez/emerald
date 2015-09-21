(ns emerald.models.client
  (:refer-clojure :exclude [get update])
  (:use
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

(defn update! [id client user-id]
  (transaction
   (let [client (prep-for-update client)]
     (when (not (empty? client))
       (update clients
               (set-fields client)
               (where {:id id}))))
   (if (:pinned client) (pin! id user-id))
   (if (= (:pinned client) false) (unpin! id user-id)))
  {:success "updated the client"})

(defn all
  ([]
   (all 10 0))
  ([lim os]
   (select clients
           (where {:deleted false})
           (limit lim)
           (offset os))))
