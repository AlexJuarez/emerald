(ns emerald.models.division
  (:refer-clojure :exclude [update get])
  (:use
   [korma.db :only (transaction)]
   [korma.core]
   [emerald.db.core]))

(defn get [id]
  (->
   (select divisions
           (where {:id id :deleted false}))
   first))

(defn exists? [id]
  (not (empty? (select divisions
                       (where {:id id :deleted false})))))

(defn prep [division]
  (assoc division :id (java.util.UUID/randomUUID)))

(defn add! [division]
  (insert divisions
          (values division)))

(defn update! [id division]
  (update divisions
          (set-fields division)
          (where {:id id}))
  {:success "updated the division"})

(defn all
  ([]
   (all 10 0))
  ([lim os]
   (select divisions
           (where {:deleted false})
           (limit lim)
           (offset os))))
