(ns emerald.models.helpers
  (:refer-clojure :exclude [update])
  (:use
   [korma.db :only (transaction)]
   [korma.core]
   [emerald.db.core]))

(defn select-ids [tbl fk ids]
  (if (empty? ids)
    []
    (->>
     (select tbl
             (fields :id)
             (where {:deleted false
                     (keyword fk) [in ids]}))
     (map #(:id %))
     (into []))))

(defn soft-delete-ids [tbl ids]
  (if (empty? ids)
    0
    (update tbl
            (set-fields {:deleted true})
            (where {:deleted false
                    :id [in ids]}))))

(defn restore-ids [tbl ids]
  (if (empty? ids)
    0
    (update tbl
            (set-fields {:deleted false})
            (where {:deleted true
                    :id [in ids]}))))
