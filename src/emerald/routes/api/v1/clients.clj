(ns emerald.routes.api.v1.clients
  (:use
   [emerald.util.core]
   [emerald.util.access])
  (:require
   [emerald.util.session :as session]
   [emerald.models.client :as client]
   [emerald.models.channel :as channel]
   [emerald.models.geo-profile :as geo-profile]
   [emerald.models.publisher :as publisher]
   [compojure.api.sweet :refer :all]
   [ring.util.http-response :refer :all]
   [schema.core :as s]))

(defn clients [limit offset]
  (client/all limit offset))

(defn get-client [id]
  (client/get id (session/get :user_id)))

(defn create-client [slug]
    (client/add! slug))

(defn update-client [id slug]
  (client/update! id slug (session/get :user_id)))

(defn get-geo-profiles [id]
  (geo-profile/all-for-client id))

(defn get-publishers [id]
  (publisher/all-for-client id))

(defn pinned-clients []
  (->> (client/all-pins (session/get :user_id))
      (map #(first (vals %)))))

(s/defschema Client
  {:channelId (s/both java.util.UUID (s/pred channel/exists? 'channel/exists?))
   :name (s/both String (s/pred client/unique-name? 'client/unique-name?))
   (s/optional-key :pinned) Boolean
   (s/optional-key :deleted) Boolean
   (s/optional-key :requireRepInfo) Boolean})

(s/defschema Edit-Client (make-optional Client))

(defn wrap-client-access [handler]
  (wrap-id-access handler client-access?))

(defroutes* client-routes
  (GET* "/clients/pinned" []
        :tags ["clients"]
        :summary "looks up a list of pinned clients"
        (ok (pinned-clients)))
  (context* "/clients/:id" []
            :tags ["clients"]
            :middlewares [wrap-client-access]
            :path-params [id :- (s/both java.util.UUID (s/pred client/exists? 'client/exists?))]
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
            )
  (GET* "/clients" []
        :tags ["clients"]
        :query-params [{limit :- Long 10} {offset :- Long 0}]
        :summary "looks up a list of clients"
        (ok (clients limit offset)))
  (POST* "/clients" []
         :tags ["clients"]
         :middlewares [wrap-employee-access]
         :body [client Client]
         :summary "creates a new client, you need special permissions to access this endpoint"
         (ok (create-client client))))
