(ns emerald.routes.api.v1.oauth
  (:use
   [emerald.util.core])
  (:require
   [emerald.models.application :as app]
   [emerald.util.session :as session]
   [emerald.models.user :as users]
   [emerald.oauth.token :as token]
   [compojure.api.sweet :refer :all]
   [ring.util.http-response :refer :all]
   [clojure.string :as st]
   [schema.core :as s]))

(s/defschema Token
  {(s/optional-key :token_type) (s/enum :bearer)
   (s/optional-key :expires_in) Long
   (s/optional-key :access_token) String
   (s/optional-key :error) String})

(defn parse-token [auth]
  (let [[type client_id client_secret] (map st/trim (filter #(not (empty? %)) (distinct (st/split auth #" "))))]
    {:client_id client_id
     :client_secret client_secret}))

(defn get-token [req]
  "Make sure that the request has an Authorization header or client_id and client_secret in the params. Binds client_id and client_secret"
  (if-let [auth (get-in req [:headers "authorization"])]
    (parse-token auth)
    (let [client_id (get-in req [:params :client_id])
          client_secret (get-in req [:params :client_secret])]
        {:client_id client_id
         :client_secret client_secret})))

(defn client-grant [token]
  (if-let [application (app/get-with-credentials (:client_id token) (:client_secret token))]
    {:token_type "bearer"
     :expires_in (* 60 60 10)
     :access_token (token/grant-for-user (:userId application))}
    {:error "Application not found, client_id or client_secret incorrect"}
    ))

(defn password-grant [req token]
  (let [username (get-in req [:params :username])
        password (get-in req [:params :password])
        user (users/login! {:username username :password password})]
    (if (nil? (:error user))
      {:token_type "bearer"
       :expires_in (* 60 60 10)
       :access_token (token/grant-for-user (:id user))}
      {:error (:error user)}
      )))

(defn grant-token [grant_type req]
  (let [token (get-token req)]
    (if (not (nil? (:client_id token)))
      (case grant_type
        :client_credentials (ok (client-grant token))
        :password (ok (password-grant req token)))
      (forbidden {:reason "authentication header or client_id and client_secret required"})
      )))

(defroutes* oauth-routes
  (context* "/oauth" []
            :tags ["oauth"]
            :summary "returns a token"
            :swagger {:parameters {:query {(s/optional-key :client_id) String
                                           (s/optional-key :client_secret) String
                                           (s/optional-key :username) String
                                           (s/optional-key :password) String}
                                    :header {(s/optional-key :authorization) String}}}
            (POST* "/token" req
                   :query-params [grant_type :- (s/enum :client_credentials :password)]
                   :responses {200 {:schema Token, :description "Valid Grant"}
                               403 {:schema {:reason String}}}
                  (grant-token grant_type req))))
