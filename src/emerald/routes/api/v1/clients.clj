(ns emerald.routes.api.v1.clients
  (:use
   [emerald.util.core])
  (:require
   [emerald.util.session :as session]
   [emerald.models.client :as client]
   [emerald.models.channel :as channel]
   [emerald.models.geo-profile :as geo-profile]
   [emerald.models.publisher :as publisher]
   [compojure.api.sweet :refer :all]
   [ring.util.http-response :refer :all]
   [schema.core :as s]))

(defn clients []
  (client/all))

(defn get-client [id]
  (client/get id))

(defn create-client [slug]
  (client/add! slug))

(defn update-client [id slug]
  (client/update! id slug))

(defn get-geo-profiles [id]
  (geo-profile/all-for-client id))

(defn get-publishers [id]
  (publisher/all-for-client id))

(defn pinned-clients []
  (client/all-pins (session/get :user_id)))

(defn create-pin [id]
  (client/pin! id (session/get :user_id))
  {"success" "client has been pinned"})

(defn delete-pin [id]
  (client/unpin! id (session/get :user_id))
  {"succss" "client has been successfully removed"})


(s/defschema Client
  {:channelId (s/both java.util.UUID (s/pred channel/exists? 'channel/exists?))
   :name String
   (s/optional-key :deleted) Boolean
   (s/optional-key :requireRepInfo) Boolean})

(s/defschema Edit-Client (make-optional Client))

(defroutes* client-routes
  (GET* "/clients/pinned" []
        :tags ["clients"]
        :summary "looks up a list of pinned clients"
        (ok (pinned-clients)))
  (context* "/clients/:id" []
            :tags ["clients"]
            :path-params [id :- java.util.UUID]
            (GET* "/" []
                  :summary "gets a client by id"
                  (ok (get-client id)))
            (GET* "/geoProfiles" []
                  :summary "gets the available geo-profiles for a client"
                  (ok (get-geo-profiles id)))
            (GET* "/publishers" []
                  :summary "gets the available publishers for a client"
                  (ok (get-publishers id)))
            (PUT* "/" []
                  :body [client Edit-Client]
                  :summary "updates a client"
                  (ok (update-client id client)))
            (POST* "/pin" []
                   :summary "pins an client for the user"
                   (ok (create-pin id)))
            (DELETE* "/pin" []
                     :summary "removes the pinned client for the user"
                     (ok (delete-pin id)))
            )
  (GET* "/clients" []
        :tags ["clients"]
        :query-params [{limit :- Long 10} {offset :- Long 0}]
        :summary "looks up a list of clients"
        (ok (clients)))
  (POST* "/clients" []
         :tags ["clients"]
         :body [client Client]
         :summary "creates a new client"
         (ok (create-client client))))
