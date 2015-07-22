(ns emerald.models.channel
  (:refer-clojure :exclude [update])
  (:use
   [korma.db :only (transaction)]
   [korma.core]
   [emerald.db.core]))

(defn exists? [id]
  (not (empty? (select channels
                  (where {:id id})))))
