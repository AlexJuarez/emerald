(ns emerald.routes.api.v1.campaigns
  (:use
   [emerald.util.core])
  (:require
   [emerald.util.session :as session]
   [emerald.models.campaign :as campaign]
   [emerald.models.account :as account]
   [emerald.models.creative :as creative]
   [emerald.models.placement :as placement]
   [compojure.api.sweet :refer :all]
   [ring.util.http-response :refer :all]
   [schema.core :as s]))

(defn campaigns [limit offset]
  (campaign/all limit offset))

(defn get-campaign [id]
  (campaign/get id))

(defn create-campaign [slug]
  (campaign/add! slug))

(defn update-campaign [id slug]
  (campaign/update! id slug))

(defn get-creatives [id]
  (creative/all-for-campaign id))

(defn get-placements [id]
  (placement/all-for-campaign id))

(defn pinned-campaigns []
  (campaign/all-pins (session/get :user_id)))

(defn create-pin [id]
  (campaign/pin! id (session/get :user_id))
  {"success" "campaign has been pinned"})

(defn delete-pin [id]
  (campaign/unpin! id (session/get :user_id))
  {"succss" "campaign has been successfully removed"})

(s/defschema Campaign
  {:accountId (s/both java.util.UUID (s/pred account/exists? 'account/exists?))
   :name String
   :startDate java.util.Date
   :endDate java.util.Date
   :repName String
   :repEmail String
   (s/optional-key :conversionDomain) String
   (s/optional-key :deleted) Boolean
   (s/optional-key :description) String
   (s/optional-key :measureReach) Boolean
   (s/optional-key :googleAnalytics) Boolean
   (s/optional-key :conversionTracking) Boolean
   (s/optional-key :objective) String
   (s/optional-key :budget) Long
   (s/optional-key :keywords) String
   (s/optional-key :clickthroughUrl) String})

(s/defschema Edit-Campaign (make-optional Campaign))

(defroutes* campaign-routes
  (GET* "/campaigns/pinned" []
        :tags ["campaigns"]
        :summary "looks up a list of pinned campaigns"
        (ok (pinned-campaigns)))
  (context* "/campaigns/:id" []
            :tags ["campaigns"]
            :path-params [id :- java.util.UUID]
            (GET* "/" []
                  :summary "gets a campaign by id"
                  (ok (get-campaign id)))
            (GET* "/creatives" []
                  :summary "gets the creatives for a campaign"
                  (ok (get-creatives id)))
            (GET* "/placements" []
                  :summary "gets the placements for a campaign"
                  (ok (get-placements id)))
            (PUT* "/" []
                  :body [campaign Edit-Campaign]
                  :summary "updates a campaign"
                  (ok (update-campaign id campaign)))
            (POST* "/pin" []
                   :summary "pins an campaign for the user"
                   (ok (create-pin id)))
            (DELETE* "/pin" []
                     :summary "removes the pinned campaign for the user"
                     (ok (delete-pin id)))
            )
  (GET* "/campaigns" []
        :tags ["campaigns"]
        :query-params [{limit :- Long 0} {offset :- Long 0}]
        :summary "looks up a list of campaigns"
        (ok (campaigns limit offset)))
  (POST* "/campaigns" []
         :tags ["campaigns"]
         :body [campaign Campaign]
         :summary "creates a new campaign"
         (ok (create-campaign campaign))))
