(ns emerald.routes.api.v1.divisions
  (:require
   [emerald.models.division :as division]
   [compojure.api.sweet :refer :all]
   [ring.util.http-response :refer :all]
   [schema.core :as s]))

(defn divisions []
  (division/all))

(defn get-division [id]
  (division/get id))

(defn create-division [slug]
  (division/add! slug))

(defn update-division [id slug]
  (division/update! id slug))

(s/defschema division
  {:name String
   (s/optional-key :deleted) Boolean
   (s/optional-key :geoProfileId) java.util.UUID
   (s/optional-key :description) String
   (s/optional-key :preferences) String})

(defroutes* division-routes
  (context* "/division/:id" []
            :tags ["divisions"]
            :path-params [id :- java.util.UUID]
            (GET* "/" []
                  :summary "gets a division by id"
                  (ok (get-division id)))
            (PUT* "/" []
                  :body [division division]
                  :summary "updates a division"
                  (ok (update-division id division))
            ))
  (GET* "/divisions" []
        :tags ["divisions"]
        :query-params [{limit :- Long 10} {offset :- Long 0}]
        :summary "looks up a list of divisions"
        (ok (divisions)))
  (POST* "/divisions" []
         :tags ["divisions"]
         :body [division division]
         :summary "creates a new division"
         (ok (create-division division))))
