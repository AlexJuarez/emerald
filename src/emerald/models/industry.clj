(ns emerald.models.industry
  (:refer-clojure :exclude [update])
  (:use
   [korma.core]
   [emerald.db.core]))

(defn exists? [id]
  (not (empty? (select industries
                  (where {:id id})))))

(defn all []
  (select industries))
