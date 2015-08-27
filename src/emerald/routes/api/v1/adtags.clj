(ns emerald.routes.api.v1.adtags
  (:require
   [emerald.models.adtag :as adtag]
   [compojure.api.sweet :refer :all]
   [ring.util.http-response :refer :all]
   [schema.core :as s]))

(defn all []
  (adtag/all))

(defroutes* adtag-routes
  (GET* "/adtags" []
        :tags ["adtags"]
        :description "returns a model representation that can be used to reconstuct the adtags"
        :summary "gets all of the adtags"
        (ok (all))))
