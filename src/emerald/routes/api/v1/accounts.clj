(ns emerald.routes.api.v1.accounts
  (:use
   [emerald.util.core])
  (:require
   [emerald.models.account :as account]
   [emerald.models.industry :as industry]
   [compojure.api.sweet :refer :all]
   [ring.util.http-response :refer :all]
   [schema.core :as s]))

(defn accounts []
  (account/all))

(defn get-account [id]
  (account/get id))

(defn create-account [slug]
  (account/add! slug))

(defn update-account [id slug]
  (account/update! id slug))

(defn create-pin [id]
  )

(defn delete-pin [id]
  )

(s/defschema Account
  {:industryId (s/both java.util.UUID (s/pred industry/exists? 'industry/exists?))
   :name String
   (s/optional-key :deleted) Boolean
   (s/optional-key :keywords) String
   (s/optional-key :clickthroughUrl) String})

(s/defschema Edit-Account (make-optional Account))

(defroutes* account-routes
  (context* "/accounts/:id" []
            :tags ["accounts"]
            :path-params [id :- java.util.UUID]
            (GET* "/" []
                  :summary "gets a account by id"
                  (ok (get-account id)))
            (PUT* "/" []
                  :body [account Edit-Account]
                  :summary "updates a account"
                  (ok (update-account id account)))
            (POST* "/pin" []
                   :summary "pins an account for the user"
                   (ok (create-pin id)))
            (DELETE* "/pin" []
                     :summary "removes the pinned account for the user"
                     (ok (delete-pin id)))
            )
  (GET* "/accounts" []
        :tags ["accounts"]
        :query-params [{limit :- Long 10} {offset :- Long 0}]
        :summary "looks up a list of accounts"
        (ok (accounts)))
  (POST* "/accounts" []
         :tags ["accounts"]
         :body [account Account]
         :summary "creates a new account"
         (ok (create-account account))))
