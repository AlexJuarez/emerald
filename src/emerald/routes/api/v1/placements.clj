(ns emerald.routes.api.v1.placements
  (:use
   [emerald.util.core]
   [emerald.util.access])
  (:require
   [emerald.models.transaction :as transaction]
   [emerald.helpers.transaction :refer [children-by-placement]]
   [emerald.models.placement :as placement]
   [emerald.models.publisher :as publisher]
   [emerald.models.campaign :as campaign]
   [emerald.models.enums :as enums]
   [clojure.test :refer :all]
   [compojure.api.sweet :refer :all]
   [ring.util.http-response :refer :all]
   [schema.core :as s]))

(defn placements [limit offset]
  (placement/all limit offset))

(defn get-placement [id]
  (placement/get id))

(defn create-placement [slug]
  (let [results (placement/add! slug)
        publisher (publisher/get (:publisherId results))]
    (merge results {:publisher publisher})))

(defn update-placement [id slug]
  (placement/update! id slug))

(defn children [id]
  (children-by-placement id))

(defn remove-placement [id]
  (let [children (children id)]
    (transaction/remove! children)))

(s/defschema Placement
  {:name String
   :publisherId (s/both java.util.UUID (s/pred publisher/exists? 'publisher/exists?))
   :campaignId (s/both java.util.UUID (s/pred campaign/exists? 'campaign/exists?) (s/pred campaign-access? 'campaign-access?))
   :flightStart java.util.Date
   :flightEnd java.util.Date
   (s/optional-key :type) (apply enums/enum-type (enums/ad-types))
   (s/optional-key :targetId) java.util.UUID
   (s/optional-key :playMode) (apply enums/enum-type (enums/play-modes))
   (s/optional-key :openLinks) (apply enums/enum-type (enums/window-types))
   (s/optional-key :clickthroughUrl) String
   (s/optional-key :clickTrackers) String
   (s/optional-key :impressionTrackers) String
   (s/optional-key :adTagId) java.util.UUID
   (s/optional-key :viewTrackers) String
   (s/optional-key :deleted) Boolean
   (s/optional-key :embedHeight) Long
   (s/optional-key :embedWidth) Long
   (s/optional-key :autoExpandEveryHours) Long
   (s/optional-key :autoCollapseAfterSeconds) Long
   (s/optional-key :takeoverLightboxOpacity) Long
   (s/optional-key :autoplayRetries) Long
   (s/optional-key :bookedImpressions) Long
   (s/optional-key :rateType) (apply enums/enum-type (enums/rate-types))
   (s/optional-key :expandType) (apply enums/enum-type (enums/expand-types))
   (s/optional-key :costPerRate) Double
   (s/optional-key :budget) Double
   (s/optional-key :expandAnchor) (apply enums/enum-type (enums/expand-anchors))
   (s/optional-key :expandDirection) (apply enums/enum-type (enums/expand-directions))
   (s/optional-key :vastMediaFileType) (apply enums/enum-type (enums/vast-media-file-types))
   (s/optional-key :vastMimeType) (apply enums/enum-type (enums/vast-mime-types))
   (s/optional-key :allowAnimations) Boolean
   (s/optional-key :skip321) Boolean
   (s/optional-key :audioOff) Boolean
   (s/optional-key :muteOnRollOut) Boolean
   (s/optional-key :collapseOnRollOut) Boolean
   (s/optional-key :expandBeforeCountdown) Boolean
   (s/optional-key :spanish) Boolean
   (s/optional-key :autoplayAudioOnRollover) Boolean
   (s/optional-key :hideClickToPlay) Boolean})

(s/defschema Placement-Children {:placement_ids [java.util.UUID]
                                 :creative_ids [java.util.UUID]})

(s/defschema Edit-Placement (make-optional Placement))

(defn wrap-placement-access [handler]
  (wrap-id-access handler placement-access?))

(defroutes* placement-routes
  (context* "/placements/:id" []
            :tags ["placements"]
            :middlewares [wrap-placement-access]
            :path-params [id :- java.util.UUID]
            (GET* "/" []
                  :summary "gets a placement by id"
                  (ok (get-placement id)))
            (PUT* "/" []
                  :body [placement Edit-Placement]
                  :summary "updates a placement"
                  (ok (update-placement id placement))
            )
            (GET* "/children" []
                  :return Placement-Children
                  :summary "gets the child ids for a account"
                  (ok (children id)))
            (DELETE* "/" []
                  :summary "deletes a account and cascades to delete all child entities"
                  (ok (remove-placement id)))
            )
  (GET* "/placements" []
        :tags ["placements"]
        :middlewares [wrap-employee-access]
        :query-params [{limit :- Long 10} {offset :- Long 0}]
        :summary "looks up a list of placements"
        (ok (placements limit offset)))
  (POST* "/placements" []
         :tags ["placements"]
         :body [placement Placement]
         :summary "creates a new placement"
         (ok (create-placement placement))))
