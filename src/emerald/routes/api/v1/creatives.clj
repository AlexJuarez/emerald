(ns emerald.routes.api.v1.creatives
  (:require
   [emerald.models.creative :as creative]
   [emerald.models.enums :as enums]
   [compojure.api.sweet :refer :all]
   [ring.util.http-response :refer :all]
   [schema.core :as s]))

(defn creatives []
  (creative/all))

(defn get-creative [id]
  (creative/get id))

(defn create-creative [slug]
  (creative/add! slug))

(defn update-creative [id slug]
  (creative/update! id slug))

(s/defschema Creative
  {:name String
   :device (apply s/enum @enums/device-types)
   :type (apply s/enum @enums/ad-types)
   (s/optional-key :embedHeight) Long
   (s/optional-key :embedWidth) Long
   (s/optional-key :keywords) String
   (s/optional-key :expandMode) (apply s/enum @enums/expand-types)
   (s/optional-key :expandAnchor) (apply s/enum @enums/expand-anchors)
   (s/optional-key :expandDirection) (apply s/enum @enums/expand-directions)
   (s/optional-key :expandedWidth) Long
   (s/optional-key :expandedHeight) Long})

(defroutes* creative-routes
  (context* "/creative/:id" []
            :tags ["creatives"]
            :path-params [id :- java.util.UUID]
            (GET* "/" []
                  :summary "gets a creative by id"
                  (ok (get-creative id)))
            (PUT* "/" []
                  :body [creative Creative]
                  :summary "updates a creative"
                  (ok (update-creative id creative))
            ))
  (GET* "/creatives" []
        :tags ["creatives"]
        :query-params [{limit :- Long 10} {offset :- Long 0}]
        :summary "looks up a list of creatives"
        (ok (creatives)))
  (POST* "/creatives" []
         :tags ["creatives"]
         :body [creative Creative]
         :summary "creates a new creative"
         (ok (create-creative creative))))
