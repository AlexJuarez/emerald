(ns emerald.models.user
  (:refer-clojure :exclude [update get])
  (:use
   [korma.db :only (transaction)]
   [korma.core]
   [emerald.db.core])
  (:require
   [emerald.util.password :as pwutil]))

(defn get-by-email [email]
  (-> (select users
          (where {:email email :deleted false}))
      first))

(defn login! [{:keys [email password] :as user}]
  (let [{:keys [salt hashed_password] :as userstore} (get-by-email email)
        user (dissoc user :password)]
    (if (nil? userstore)
      (assoc user :error "Username does not exist")
      (if (pwutil/equals? hashed_password (str password salt))
        (dissoc userstore :hashed_password :salt)
        (assoc user :error "Password Incorrect.")))))
