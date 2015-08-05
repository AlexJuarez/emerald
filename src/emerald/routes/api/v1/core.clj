(ns emerald.routes.api.v1.core
  (:require
   [emerald.routes.api.v1.placements :refer [placement-routes]]
   [emerald.routes.api.v1.clients :refer [client-routes]]
   [emerald.routes.api.v1.accounts :refer [account-routes]]
   [emerald.routes.api.v1.campaigns :refer [campaign-routes]]
   [emerald.routes.api.v1.creatives :refer [creative-routes]]
   [emerald.routes.api.v1.publishers :refer [publisher-routes]]
   [emerald.routes.api.v1.divisions :refer [division-routes]]
   [emerald.routes.api.v1.industries :refer [industry-routes]]
   [emerald.routes.api.v1.enums :refer [enum-routes]]
   [emerald.routes.api.v1.channels :refer [channel-routes]]
   [emerald.routes.api.v1.adtags :refer [adtag-routes]]
   [emerald.routes.api.v1.oauth :refer [oauth-routes]]
   [emerald.middleware :as middleware]
   [compojure.api.sweet :refer :all]
   [ring.util.http-response :refer :all]
   [schema.core :as s]))

(defapi api-routes
  {:formats [:json-kw]}
  (swagger-ui
   "/docs"
   :swagger-docs "/docs.json") ;;Change swagger.json endpoint
  (swagger-docs
   "/docs.json"
   {:info {:title "CRUD API V1"}})
  oauth-routes
  (middlewares [middleware/wrap-api-restricted]
               placement-routes
               client-routes
               account-routes
               campaign-routes
               creative-routes
               division-routes
               publisher-routes
               adtag-routes
               industry-routes
               enum-routes
               channel-routes))
