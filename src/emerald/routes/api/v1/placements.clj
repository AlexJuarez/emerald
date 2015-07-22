(ns emerald.routes.api.v1.placements
  (:require
   [emerald.models.placement :as placement]
   [clojure.test :refer :all]
   [compojure.api.sweet :refer :all]
   [ring.util.http-response :refer :all]
   [schema.core :as s]))

(defn placements []
  (placement/all))

(defn get-placement [id]
  (placement/get id))

(defroutes* placement-routes
  (GET* "/placement/:id" [id]
       :path-params [id :- java.util.UUID]
       (ok (get-placement id)))
  (GET* "/placements" []
        :query-params [{limit :- Long 10} {offset :- Long 0}]
        (ok (placements))))
