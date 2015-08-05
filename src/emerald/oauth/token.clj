(ns emerald.oauth.token
  (:refer-clojure :exclude [get])
  (:require
   [crypto.random :as random]
   [emerald.cache :as cache]
   [emerald.util.session :as session]))

(defn generate-token []
  "generates a unique token"
  (random/base64 20))

(defn grant-for-user [user-id]
  (let [t (session/get-in [:session :accessToken])]
    (if (nil? t)
      (let [t (generate-token)]
        (cache/set (str "oauth:" t) {:user_id user-id} (* 60 60 10))
        (session/put! :accessToken t)
        t)
      t)))
