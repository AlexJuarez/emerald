(ns emerald.routes.api.v1.channels
  (:require
   [emerald.models.channel :as channel]
   [compojure.api.sweet :refer :all]
   [ring.util.http-response :refer :all]
   [schema.core :as s]))

(defn channels []
  (channel/all))

(defroutes* channel-routes
  (GET* "/channels" []
        :tags ["channels"]
        :summary "looks up a list of channels"
        (ok (channels))))
