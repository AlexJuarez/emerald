(ns emerald.routes.api.v1.core
  (:require
   [emerald.routes.api.v1.placements :refer [placement-routes]]
   [emerald.routes.api.v1.clients :refer [client-routes]]
   [compojure.api.sweet :refer :all]
   [ring.util.http-response :refer :all]
   [schema.core :as s]))


(defapi api-routes
  {:formats [:json-kw]}
  (swagger-ui
   "/api/v1/docs"
   :swagger-docs "/api/v1/docs.json") ;;Change swagger.json endpoint
  (swagger-docs
   "/api/v1/docs.json"
   {:info {:title "CRUD API V1"}})
  (context "/api/v1" []
           placement-routes
           client-routes))
