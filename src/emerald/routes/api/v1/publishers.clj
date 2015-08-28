(ns emerald.routes.api.v1.publishers
  (:use
   [emerald.util.core]
   [emerald.util.access])
  (:require
   [emerald.models.publisher :as publisher]
   [emerald.models.client :as client]
   [emerald.models.enums :as enums]
   [compojure.api.sweet :refer :all]
   [ring.util.http-response :refer :all]
   [schema.core :as s]))

(defn publishers [limit offset]
  (publisher/all limit offset))

(defn get-publisher [id]
  (publisher/get id))

(defn create-publisher [slug]
  (publisher/add! slug))

(defn update-publisher [id slug]
  (publisher/update! id slug))

(s/defschema Publisher
  {:name String
   :clientId (s/both java.util.UUID (s/pred client/exists? 'client/exists?) (s/pred client-access? 'client-access?))
   (s/optional-key :skip321)  Boolean
   (s/optional-key :audioOff) Boolean
   (s/optional-key :playMode) (apply s/enum (enums/play-modes))
   (s/optional-key :hotspot) Boolean
   (s/optional-key :allowAnimations) Boolean
   (s/optional-key :muteOnRollOut) Boolean
   (s/optional-key :openLinks) (apply s/enum (enums/window-types))})

(s/defschema Edit-Publisher (make-optional Publisher))

(defn wrap-publisher-access [handler]
  (wrap-id-access handler publisher-access?))

(defroutes* publisher-routes
  (context* "/publishers/:id" []
            :tags ["publishers"]
            :middlewares [wrap-publisher-access]
            :path-params [id :- java.util.UUID]
            (GET* "/" []
                  :summary "gets a publisher by id"
                  (ok (get-publisher id)))
            (PUT* "/" []
                  :body [publisher Edit-Publisher]
                  :summary "updates a publisher"
                  (ok (update-publisher id publisher))
            ))
  (GET* "/publishers" []
        :tags ["publishers"]
        :middlewares [wrap-employee-access]
        :query-params [{limit :- Long 10} {offset :- Long 0}]
        :summary "looks up a list of publishers"
        (ok (publishers limit offset)))
  (POST* "/publishers" []
         :tags ["publishers"]
         :body [publisher Publisher]
         :summary "creates a new publisher"
         (ok (create-publisher publisher))))
