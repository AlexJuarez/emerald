(ns emerald.models.user
  (:refer-clojure :exclude [update get])
  (:use
   [emerald.db.predicates]
   [korma.db :only (transaction)]
   [korma.core]
   [emerald.db.core])
  (:require
   [clojure.string :as s]
   [emerald.util.password :as pwutil]))

(defn get [id]
  (-> (select users
              (where {:id id :deleted false}))
      first))

(defn get-by-email-or-username [username]
  (let [username (s/lower-case username)]
    (-> (select users
                (where (or {:email [ilike username]}
                           {:name [ilike username]})))
        first)))

(defn login! [{:keys [username password] :as user}]
  (let [{:keys [salt hashed_password] :as userstore} (get-by-email-or-username username)
        user (dissoc user :password)]
    (if (nil? userstore)
      (assoc user :error "Username or Email does not exist")
      (if (pwutil/equals? hashed_password (str password salt))
        (dissoc userstore :hashed_password :salt)
        (assoc user :error "Password Incorrect.")))))
