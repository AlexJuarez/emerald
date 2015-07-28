(ns emerald.routes.api.v1.publishers
  (:require
   [emerald.models.publisher :as publisher]
   [emerald.models.enums :as enums]
   [compojure.api.sweet :refer :all]
   [ring.util.http-response :refer :all]
   [schema.core :as s]))

(defn publishers []
  (publisher/all))

(defn get-publisher [id]
  (publisher/get id))

(defn create-publisher [slug]
  (publisher/add! slug))

(defn update-publisher [id slug]
  (publisher/update! id slug))

(s/defschema Publisher
  {:name String
   (s/optional-key :skip321)  Boolean
   (s/optional-key :audioOff) Boolean
   (s/optional-key :playMode) (apply s/enum enums/play-modes)
   (s/optional-key :hotspot) Boolean
   (s/optional-key :allowAnimations) Boolean
   (s/optional-key :muteOnRollOut) Boolean
   (s/optional-key :openLinks) (apply s/enum enums/window-types)})

(defroutes* publisher-routes
  (context* "/publisher/:id" []
            :tags ["publishers"]
            :path-params [id :- java.util.UUID]
            (GET* "/" []
                  :summary "gets a publisher by id"
                  (ok (get-publisher id)))
            (PUT* "/" []
                  :body [publisher Publisher]
                  :summary "updates a publisher"
                  (ok (update-publisher id publisher))
            ))
  (GET* "/publishers" []
        :tags ["publishers"]
        :query-params [{limit :- Long 10} {offset :- Long 0}]
        :summary "looks up a list of publishers"
        (ok (publishers)))
  (POST* "/publishers" []
         :tags ["publishers"]
         :body [publisher Publisher]
         :summary "creates a new publisher"
         (ok (create-publisher publisher))))
