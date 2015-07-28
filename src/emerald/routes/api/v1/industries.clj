(ns emerald.routes.api.v1.industries
  (:require
   [emerald.models.industry :as industry]
   [compojure.api.sweet :refer :all]
   [ring.util.http-response :refer :all]
   [schema.core :as s]))

(defn industries []
  (industry/all))

(defroutes* industry-routes
  (GET* "/industries" []
        :tags ["industries"]
        :summary "looks up a list of industries"
        (ok (industries))))
