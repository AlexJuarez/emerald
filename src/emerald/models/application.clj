(ns emerald.models.application
  (:refer-clojure :exclude [update get])
  (:use
   [korma.db :only (transaction)]
   [korma.core]
   [emerald.db.core]))

(defn all-for-user [user-id]
  (select applications
          (where {:user_id user-id})))
