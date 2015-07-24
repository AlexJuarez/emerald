(ns emerald.routes.api.v1.clients
  (:require
   [emerald.models.client :as client]
   [emerald.models.channel :as channel]
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

(s/defschema Client
  {:channelId (s/both java.util.UUID (s/pred channel/exists? 'channel/exists?))
   :name String
   (s/optional-key :requireRepInfo) Boolean})

(defroutes* client-routes
  (context* "/client/:id" []
            :tags ["clients"]
            :path-params [id :- java.util.UUID]
            (GET* "/" []
                  :summary "gets a client by id"
                  (ok (get-client id)))
            (PUT* "/" []
                  :body [client Client]
                  :summary "updates a client"
                  (ok (update-client id client))
            ))
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
