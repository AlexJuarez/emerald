(ns emerald.routes.api.v1.divisions
  (:use
   [emerald.util.core]
   [emerald.util.access])
  (:require
   [emerald.models.transaction :as transaction]
   [emerald.helpers.transaction :refer [children-by-division]]
   [emerald.util.session :as session]
   [emerald.models.division :as division]
   [emerald.models.client :as client]
   [compojure.api.sweet :refer :all]
   [ring.util.http-response :refer :all]
   [schema.core :as s]))

(defn divisions [limit offset]
  (division/all limit offset))

(defn get-division [id]
  (division/get id (session/get :user_id)))

(defn create-division [slug]
  (division/add! slug))

(defn update-division [id slug]
  (division/update! id slug (session/get :user_id)))

(defn pinned-divisions []
  (->> (division/all-pins (session/get :user_id))
       (map #(first (vals %)))))

(defn children [id]
  (children-by-division id))

(defn remove-division [id]
  (let [children (children id)]
    (transaction/remove! children)))

(s/defschema Division
  {:name String
   :clientId (s/both java.util.UUID (s/pred client/exists? 'client/exists?) (s/pred client-access? 'client-access?))
   (s/optional-key :pinned) Boolean
   (s/optional-key :deleted) Boolean
   (s/optional-key :geoProfileId) java.util.UUID
   (s/optional-key :description) String
   (s/optional-key :preferences) String})

(s/defschema Division-Children {:division_ids [java.util.UUID]
                                :account_ids [java.util.UUID]
                                :campaign_ids [java.util.UUID]
                                :placement_ids [java.util.UUID]
                                :creative_ids [java.util.UUID]})

(s/defschema Edit-Division (make-optional Division))

(defn wrap-division-access [handler]
  (wrap-id-access handler division-access?))

(defroutes* division-routes
  (GET* "/divisions/pinned" []
        :tags ["divisions"]
        :summary "looks up a list of pinned divisions"
        (ok (pinned-divisions)))
  (context* "/divisions/:id" []
            :tags ["divisions"]
            :middlewares [wrap-division-access]
            :path-params [id :- (s/both java.util.UUID (s/pred division/exists? 'division/exists?))]
            (GET* "/" []
                  :summary "gets a division by id"
                  (ok (get-division id)))
            (GET* "/children" []
                  :return Division-Children
                  :summary "gets the child ids for a division"
                  (ok (children id)))
            (PUT* "/" []
                  :body [division Edit-Division]
                  :summary "updates a division"
                  (ok (update-division id division)))
            (DELETE* "/" []
                  :summary "deletes a division and cascades to delete all child entities"
                  (ok (remove-division id)))
            )
  (GET* "/divisions" []
        :tags ["divisions"]
        :middlewares [wrap-employee-access]
        :query-params [{limit :- Long 10} {offset :- Long 0}]
        :summary "looks up a list of divisions"
        (ok (divisions limit offset)))
  (POST* "/divisions" []
         :tags ["divisions"]
         :body [division Division]
         :summary "creates a new division"
         (ok (create-division division))))
