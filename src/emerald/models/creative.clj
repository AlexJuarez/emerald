(ns emerald.models.creative
  (:refer-clojure :exclude [update get])
  (:use
   [korma.core]
   [emerald.db.core]))

(defn get [id]
  (->
   (select creatives
           (where {:id id}))
   first))

(defn exists? [id]
  (not (empty? (select creatives
                       (where {:id id})))))

(defn prep [creative]
  (assoc creative :id (java.util.UUID/randomUUID)))

(defn add! [creative]
  (insert creatives
          (values creative)))

(defn update! [id creative]
  (update creatives
          (set-fields creative)
          (where {:id id}))
  {:success "updated the creative"})

(defn all
  ([]
   (all 10 0))
  ([lim os]
   (select creatives
           (limit lim)
           (offset os))))
