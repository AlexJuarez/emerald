(ns emerald.models.publisher
  (:refer-clojure :exclude [update get])
  (:use
   [korma.db :only (transaction)]
   [korma.core]
   [emerald.db.core]))

(defn get [id]
  (->
   (select publishers
           (with industries
                 (fields [:name :industry.name]))
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
  {:success "updated the publisher"})

(defn all
  ([]
   (all 10 0))
  ([lim os]
   (select publishers
           (limit lim)
           (offset os))))
