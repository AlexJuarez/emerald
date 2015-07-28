(ns emerald.routes.api.v1.campaigns
  (:require
   [emerald.models.campaign :as campaign]
   [emerald.models.account :as account]
   [emerald.models.creative :as creative]
   [compojure.api.sweet :refer :all]
   [ring.util.http-response :refer :all]
   [schema.core :as s]))

(defn campaigns []
  (campaign/all))

(defn get-campaign [id]
  (campaign/get id))

(defn create-campaign [slug]
  (campaign/add! slug))

(defn update-campaign [id slug]
  (campaign/update! id slug))

(defn get-creatives [id]
  (creative/all-for-campaign id))

(s/defschema Campaign
  {:accountId (s/both java.util.UUID (s/pred account/exists? 'account/exists?))
   :name String
   :startDate java.util.Date
   :endDate java.util.Date
   :repName String
   :repEmail String
   (s/optional-key :deleted) Boolean
   (s/optional-key :description) String
   (s/optional-key :measureReach) Boolean
   (s/optional-key :googleAnalytics) Boolean
   (s/optional-key :conversionTracking) Boolean
   (s/optional-key :objective) String
   (s/optional-key :budget) Long
   (s/optional-key :keywords) String
   (s/optional-key :clickthroughUrl) String})

(defroutes* campaign-routes
  (context* "/campaign/:id" []
            :tags ["campaigns"]
            :path-params [id :- java.util.UUID]
            (GET* "/" []
                  :summary "gets a campaign by id"
                  (ok (get-campaign id)))
            (GET* "/creatives" []
                  :summary "gets the cratives for a campaign"
                  (ok (get-creatives id)))
            (PUT* "/" []
                  :body [campaign Campaign]
                  :summary "updates a campaign"
                  (ok (update-campaign id campaign))
            ))
  (GET* "/campaigns" []
        :tags ["campaigns"]
        :query-params [{limit :- Long 0} {offset :- Long 0} {dimensions :- String ""}]
        :summary "looks up a list of campaigns"
        (ok (campaigns)))
  (POST* "/campaigns" []
         :tags ["campaigns"]
         :body [campaign Campaign]
         :summary "creates a new campaign"
         (ok (create-campaign campaign))))
