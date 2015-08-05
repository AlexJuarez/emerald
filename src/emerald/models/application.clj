(ns emerald.models.application
  (:refer-clojure :exclude [update get])
  (:use
   [korma.db :only (get-connection transaction)]
   [korma.core]
   [emerald.db.core])
  (:require [clojure.string :as s]
            [emerald.cache :as cache]
            [emerald.util.session :as session]
            [emerald.oauth.token :as token]))

(defn get [id user-id]
  (first (select applications
                 (where {:id id :user_id user-id}))))

(defn get-with-credentials [appKey appSecret]
  (-> (select applications
              (where {:app_key appKey :app_secret appSecret}))
      first))

(defn get-app-id [id]
  (select applications
          (where {:app_key id})))

(defn all-for-user [user-id]
  (select applications
          (where {:user_id user-id})))

(defn prep [{:keys [name description website callbackUrl] :as slug} user-id]
  {:name name
   :description description
   :website website
   :callbackUrls (into-array String (map s/trim (s/split callbackUrl #",")))
   :appKey (token/generate-token)
   :appSecret (token/generate-token)
   :userId user-id
   })

(defn add! [slug user-id]
  (insert applications
          (values (prep slug user-id))))
