(ns emerald.util.access
  (:require [emerald.util.session :as sess]
            [buddy.auth.middleware :refer [wrap-authentication]]
            [buddy.auth.accessrules :refer [restrict]]
            [taoensso.timbre :as log]
            [cheshire.core :as json]
            [emerald.models.division :as division]
            [emerald.models.campaign :as campaign]
            [emerald.models.creative :as creative]
            [emerald.models.placement :as placement]
            [emerald.models.publisher :as publisher]
            [emerald.models.account :as account]))

(defn on-error [request response]
  {:status  200
   :headers {"Content-Type" "application/json"}
   :body    (json/encode {:error "insufficient permissions"})})

(defmacro access-wrapper [f]
  `(fn [request#]
     (let [id# (try
                 (java.util.UUID/fromString (get (:params request#) :id))
                 (catch Exception e#
                   (when (not (= (type e#) java.lang.NullPointerException)) (log/error "id cast error" e#))
                   ""))
                 ]
       (~f id#))))

(defmacro wrap-id-access [handler access]
  `(restrict ~handler {:handler (access-wrapper ~access)
                      :on-error on-error}))

(defmacro access? [f id]
  `(= true
      (or (:employee (sess/get :user))
          (~f ~id (sess/get :user)))))

(defn employee-access? [& id]
  (= true (:employee (sess/get :user))))

(defn client-access?
  "Does the user have access to the client"
  [id]
  (access?
   (fn [id user-id]
     (= (sess/get-in [:user :client_id])
       id))
   id))

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
     (let [campaign-id (:campaignId (creative/get id))]
       (campaign-access? campaign-id)))
   id))

(defn placement-access?
  "Does the user have access to a placement"
  [id]
  (access?
   (fn [id]
     (let [campaign-id (:campaignId (placement/get id))]
       (campaign-access? campaign-id)))
   id))

(defn publisher-access?
  "Does the user have access to a publisher"
  [id]
  (access?
   (fn [id]
     (let [client-id (:clientId (publisher/get id))]
       (client-access? client-id)))
   id))

(defn wrap-employee-access [handler]
  (wrap-id-access handler employee-access?))
