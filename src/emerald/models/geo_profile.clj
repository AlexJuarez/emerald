(ns emerald.models.geo-profile
  (:refer-clojure :exclude [update])
  (:use
   [korma.core]
   [emerald.db.core]))

(defn all-for-client
  ([client-id]
   (select geo-profiles
           (where {:client_id client-id}))))
