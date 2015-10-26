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
   [emerald.routes.api.v1.upload :refer [upload-routes]]
   [emerald.middleware :as middleware]
   [emerald.util.exception :refer [request-validation-handler default-handler]]
   [compojure.api.sweet :refer :all]
   [emerald.util.enums :refer [string->enum]]
   [emerald.db.protocols]
   [schema.core :as s]
   [schema.coerce :as sc]
   [ring.swagger.coerce :as rsc]))

(defn keyword-enum-matcher [schema]
  (when (and (instance? emerald.models.enums.KormaEnumSchema schema)
             (every? keyword? (.-vs ^emerald.models.enums.KormaEnumSchema schema)))
    string->enum))

(defn json-schema-korma-coercion-matcher
  [schema]
  (or (rsc/json-coersions schema)
      (keyword-enum-matcher schema)
      (rsc/set-matcher schema)
      (rsc/set-matcher schema)
      (rsc/date-time-matcher schema)
      (rsc/date-matcher schema)
      (rsc/pattern-matcher schema)))

(defn coercion-matchers [_]
  {:body json-schema-korma-coercion-matcher
   :string rsc/query-schema-coercion-matcher
   :response rsc/json-schema-coercion-matcher})

(defapi api-routes
  {:format {:formats [:json-kw]}
   :exceptions {:handlers {:compojure.api.exception/default default-handler
                           :compojure.api.exception/request-validation request-validation-handler}}
   :coercion coercion-matchers}
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
               upload-routes
               enum-routes
               channel-routes))
