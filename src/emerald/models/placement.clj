(ns emerald.models.placement
  (:refer-clojure :exclude [get update])
  (:use
   [korma.db :only (transaction)]
   [korma.core]
   [emerald.db.core]))

(defn get [id]
  (->
   (select placements
           (with publishers
                 (fields [:name :publisher.name] [:id :publisher.id]))
           (with creatives
                 (fields :id :name))
           (where {:id id}))
   first
   ))

(defn prep [placement]
  (assoc placement :id (java.util.UUID/randomUUID)))

(defn add! [placement]
  (insert placements
          (values placement)))

(defn update! [id placement]
  (update placements
          (set-fields placement)
          (where {:id id}))
  {:success "updated the placement"})

(defn all
  ([]
   (all 10 0))
  ([lim os]
   (select placements
           (limit lim)
           (offset os))))
