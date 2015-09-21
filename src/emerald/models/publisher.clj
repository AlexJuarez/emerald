(ns emerald.models.publisher
  (:refer-clojure :exclude [update get])
  (:use
   [korma.db :only (transaction)]
   [korma.core]
   [emerald.db.core]))

(defn get [id]
  (->
   (select publishers
           (where {:id id}))
   first))

(defn exists? [id]
  (not (empty? (select publishers
                       (where {:id id})))))

(defn prep [publisher]
  (assoc publisher :id (java.util.UUID/randomUUID)))

(defn add! [publisher]
  (insert publishers
          (values publisher)))

(defn update! [id publisher]
  (update publishers
          (set-fields publisher)
          (where {:id id}))
  (get id))

(defn all
  ([]
   (all 10 0))
  ([lim os]
   (select publishers
           (limit lim)
           (offset os))))

(defn all-for-client
  [client-id]
  (select publishers
          (where {:client_id client-id})))
