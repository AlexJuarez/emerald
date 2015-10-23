(ns emerald.routes.api.v1.creatives
  (:use
   [emerald.util.core]
   [emerald.util.access])
  (:require
   [emerald.models.creative :as creative]
   [emerald.models.campaign :as campaign]
   [emerald.models.enums :as enums]
   [compojure.api.sweet :refer :all]
   [ring.util.http-response :refer :all]
   [schema.core :as s]))

(defn creatives [limit offset]
  (creative/all limit offset))

(defn get-creative [id]
  (creative/get id))

(defn create-creative [slug]
  (creative/add! slug))

(defn update-creative [id slug]
  (let [checks (some #{:name :campaignId} (keys slug))]
    (if (and
         (not (nil? checks))
         (not (creative/unique? slug id)))
      (bad-request {:errors {:name "provided name is the same as another name in this campaign"}})
      (ok (creative/update! id slug)))))

(s/defschema Creative
  {:name String
   :campaignId (s/both java.util.UUID (s/pred campaign/exists? 'campaign/exists?) (s/pred campaign-access? 'campaign-access?))
   :device (apply enums/enum-type (enums/device-types))
   :type (apply enums/enum-type (enums/ad-types))
   :clickthroughUrl String
   (s/optional-key :deleted) Boolean
   (s/optional-key :embedHeight) Long
   (s/optional-key :embedWidth) Long
   (s/optional-key :keywords) String
   (s/optional-key :mediaId) java.util.UUID
   (s/optional-key :expandType) (apply enums/enum-type (enums/expand-types))
   (s/optional-key :expandAnchor) (apply enums/enum-type (enums/expand-anchors))
   (s/optional-key :expandDirection) (apply enums/enum-type (enums/expand-directions))
   (s/optional-key :expandedWidth) Long
   (s/optional-key :expandedHeight) Long})

(s/defschema Edit-Creative (make-optional Creative))
(s/defschema Create-Creative (s/both Creative (s/pred creative/unique? 'creative/unique?)))

(defn wrap-creative-access [handler]
  (wrap-id-access handler creative-access?))

(defroutes* creative-routes
  (context* "/creatives/:id" []
            :tags ["creatives"]
            :middlewares [wrap-creative-access]
            :path-params [id :- java.util.UUID]
            (GET* "/" []
                  :summary "gets a creative by id"
                  (ok (get-creative id)))
            (PUT* "/" []
                  :body [creative Edit-Creative]
                  :summary "updates a creative"
                  (update-creative id creative)
            ))
  (GET* "/creatives" []
        :tags ["creatives"]
        :middlewares [wrap-employee-access]
        :query-params [{limit :- Long 10} {offset :- Long 0}]
        :summary "looks up a list of creatives"
        (ok (creatives limit offset)))
  (POST* "/creatives" []
         :tags ["creatives"]
         :body [creative Create-Creative]
         :summary "creates a new creative"
         (ok (create-creative creative))))
