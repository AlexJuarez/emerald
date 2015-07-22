(ns emerald.models.channel
  (:refer-clojure :exclude [update])
  (:use
   [korma.core]
   [emerald.db.core]))

(defn exists? [id]
  (not (empty? (select channels
                  (where {:id id})))))
