(ns emerald.routes.api.v1.divisions
  (:use
   [emerald.util.core])
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

(defn create-pin [id]
  )

(defn delete-pin [id]
  )

(s/defschema Division
  {:name String
   (s/optional-key :deleted) Boolean
   (s/optional-key :geoProfileId) java.util.UUID
   (s/optional-key :description) String
   (s/optional-key :preferences) String})

(s/defschema Edit-Division (make-optional Division))

(defroutes* division-routes
  (context* "/divisions/:id" []
            :tags ["divisions"]
            :path-params [id :- java.util.UUID]
            (GET* "/" []
                  :summary "gets a division by id"
                  (ok (get-division id)))
            (PUT* "/" []
                  :body [division Edit-Division]
                  :summary "updates a division"
                  (ok (update-division id division)))
            (POST* "/pin" []
                   :summary "pins an division for the user"
                   (ok (create-pin id)))
            (DELETE* "/pin" []
                     :summary "removes the pinned division for the user"
                     (ok (delete-pin id)))
            )
  (GET* "/divisions" []
        :tags ["divisions"]
        :query-params [{limit :- Long 10} {offset :- Long 0}]
        :summary "looks up a list of divisions"
        (ok (divisions)))
  (POST* "/divisions" []
         :tags ["divisions"]
         :body [division Division]
         :summary "creates a new division"
         (ok (create-division division))))
