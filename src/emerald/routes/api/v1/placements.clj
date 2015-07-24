(ns emerald.routes.api.v1.placements
  (:require
   [emerald.models.placement :as placement]
   [emerald.models.publisher :as publisher]
   [emerald.models.enums :as enums]
   [clojure.test :refer :all]
   [compojure.api.sweet :refer :all]
   [ring.util.http-response :refer :all]
   [schema.core :as s]))

(defonce play-modes (atom (map keyword (enums/play-modes))))
(defonce window-types (atom (map keyword (enums/window-types))))
(defonce ad-types (atom (map keyword (enums/ad-types))))

(defn placements []
  (placement/all))

(defn get-placement [id]
  (placement/get id))

(defn create-placement [slug]
  (placement/add! slug))

(defn update-placement [id slug]
  (placement/update! id slug))

(s/defschema Placement
  {:name String
   :publisherId (s/both java.util.UUID (s/pred publisher/exists? 'publisher/exists?))
   :playMode (apply s/enum @play-modes)
   :openLinks (apply s/enum @window-types)
   :flightStart java.util.Date
   :flightEnd java.util.Date
   :type (apply s/enum @ad-types)
   (s/optional-key :bookedImpressions) Long
   (s/optional-key :cost) Long
   (s/optional-key :allowAnimations) Boolean
   (s/optional-key :skip321) Boolean
   (s/optional-key :audioOff) Boolean
   (s/optional-key :muteOnRollOut) Boolean
   })

(defroutes* placement-routes
  (context* "/placement/:id" []
            :tags ["placements"]
            :path-params [id :- java.util.UUID]
            (GET* "/" []
                  :summary "gets a placement by id"
                  (ok (get-placement id)))
            (PUT* "/" []
                  :body [placement Placement]
                  :summary "updates a placement"
                  (ok (update-placement id placement))
            ))
  (GET* "/placements" []
        :tags ["placements"]
        :query-params [{limit :- Long 10} {offset :- Long 0}]
        :summary "looks up a list of placements"
        (ok (placements)))
  (POST* "/placements" []
         :tags ["placements"]
         :body [placement Placement]
         :summary "creates a new placement"
         (ok (create-placement placement))))
