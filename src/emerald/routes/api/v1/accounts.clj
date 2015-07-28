(ns emerald.routes.api.v1.accounts
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

(s/defschema Account
  {:industryId (s/both java.util.UUID (s/pred industry/exists? 'industry/exists?))
   :name String
   (s/optional-key :deleted) Boolean
   (s/optional-key :keywords) String
   (s/optional-key :clickthroughUrl) String})

(defroutes* account-routes
  (context* "/account/:id" []
            :tags ["accounts"]
            :path-params [id :- java.util.UUID]
            (GET* "/" []
                  :summary "gets a account by id"
                  (ok (get-account id)))
            (PUT* "/" []
                  :body [account Account]
                  :summary "updates a account"
                  (ok (update-account id account))
            ))
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
