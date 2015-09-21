(ns emerald.routes.api.v1.accounts
  (:use
   [emerald.util.core]
   [emerald.util.access])
  (:require
   [emerald.util.session :as session]
   [emerald.models.account :as account]
   [emerald.models.industry :as industry]
   [emerald.models.division :as division]
   [compojure.api.sweet :refer :all]
   [ring.util.http-response :refer :all]
   [schema.core :as s]))

(defn accounts [limit offset]
  (account/all limit offset))

(defn get-account [id]
  (account/get id (session/get :user_id)))

(defn create-account [slug]
  (account/add! slug))

(defn update-account [id slug]
  (account/update! id slug (session/get :user_id)))

(defn pinned-accounts []
  (account/all-pins (session/get :user_id)))

(s/defschema Account
  {:industryId (s/both java.util.UUID (s/pred industry/exists? 'industry/exists?))
   :divisionId (s/both java.util.UUID (s/pred division/exists? 'division/exists?) (s/pred division-access? 'division-access?))
   :name String
   (s/optional-key :pinned) Boolean
   (s/optional-key :leadCaptureEmail) String
   (s/optional-key :deleted) Boolean
   (s/optional-key :keywords) String
   (s/optional-key :clickthroughUrl) String})

(s/defschema Edit-Account (make-optional Account))

(defn wrap-account-access [handler]
  (wrap-id-access handler account-access?))

(defroutes* account-routes
  (GET* "/accounts/pinned" []
        :tags ["accounts"]
        :summary "looks up a list of pinned accounts"
        (ok (pinned-accounts)))
  (context* "/accounts/:id" []
            :tags ["accounts"]
            :middlewares [wrap-account-access]
            :path-params [id :- (s/both java.util.UUID (s/pred account/exists? 'account/exists?))]
            (GET* "/" []
                  :summary "gets a account by id"
                  (ok (get-account id)))
            (PUT* "/" []
                  :body [account Edit-Account]
                  :summary "updates a account"
                  (ok (update-account id account)))
            )
  (GET* "/accounts" []
        :tags ["accounts"]
        :middlewares [wrap-employee-access]
        :query-params [{limit :- Long 10} {offset :- Long 0}]
        :summary "looks up a list of accounts"
        (ok (accounts limit offset)))
  (POST* "/accounts" []
         :tags ["accounts"]
         :body [account Account]
         :summary "creates a new account"
         (ok (create-account account))))
