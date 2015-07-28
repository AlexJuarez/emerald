(ns emerald.routes.api.v1.enums
  (:require
   [emerald.models.enums :as enums]
   [compojure.api.sweet :refer :all]
   [ring.util.http-response :refer :all]
   [schema.core :as s]))

(defn all []
  {:deviceTypes @enums/device-types
   :adTypes @enums/ad-types
   :expandAnchors @enums/expand-anchors
   :expandDirections @enums/expand-directions
   :expandTypes @enums/expand-types
   :playModes @enums/play-modes
   :windowTypes @enums/window-types
   })

(defroutes* enum-routes
  (GET* "/enums" []
        :tags ["enums"]
        :summary "gets all of the enums"
        (ok (all))))
