(ns emerald.util.access
  (:require [emerald.util.session :as sess]
            [emerald.models.division :as division]
            [emerald.models.campaign :as campaign]
            [emerald.models.creative :as creative]

            [emerald.models.account :as account]))

(defmacro access? [f id]
  `(= true
      (or (:employee (sess/get :user))
          (~f ~id (sess/get :user)))))

(defn client-access?
  "Does the user have access to the client"
  [id]
  (access?
   (fn [id user-id]
     (= (sess/get-in [:user :client_id])
       id))
   id)
   )

(defn division-access?
  "Does the user have access to the division"
  [id]
  (= true
     (or
      (access?
       division/access?
       id)
      (let
        [client-id (:clientId (division/get id))]
        (and
         (client-access? client-id)
         (sess/get-in [:user :admin]))))))

(defn account-access?
  "Does the user have access to the account"
  [id]
  (= true
     (or
      (access?
       account/access?
       id)
      (let
        [division-id (:divisionId (account/get id))]
        (division-access? division-id)))))

(defn campaign-access?
  "Does the user have access to the campaign"
  [id]
  (access?
     (fn [id]
       (let [account-id (:accountId (campaign/get id))]
         (account-access? account-id)))
   id))

(defn creative-access?
  "Does the user have access to a creative"
  [id]
  (access?
   (fn [id]
     (let [campaign-id (creative/get id)]
       (campaign-access? campaign-id)))
   id))



