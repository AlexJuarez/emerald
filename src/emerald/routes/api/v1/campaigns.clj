(ns emerald.routes.api.v1.campaigns
  (:require
   [emerald.models.campaign :as campaign]
   [emerald.models.account :as account]
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

(s/defschema Campaign
  {:accountId (s/both java.util.UUID (s/pred account/exists? 'account/exists?))
   :name String
   (s/optional-key :keywords) String
   (s/optional-key :clickthroughUrl) String})

(defroutes* campaign-routes
  (context* "/campaign/:id" []
            :path-params [id :- java.util.UUID]
            (GET* "/" []
                  :summary "gets a campaign by id"
                  (ok (get-campaign id)))
            (PUT* "/" []
                  :body [campaign Campaign]
                  :summary "updates a campaign"
                  (ok (update-campaign id campaign))
            ))
  (GET* "/campaigns" []
        :query-params [{limit :- Long 10} {offset :- Long 0}]
        :summary "looks up a list of campaigns"
        (ok (campaigns)))
  (POST* "/campaigns" []
         :body [campaign Campaign]
         :summary "creates a new campaign"
         (ok (create-campaign campaign))))
