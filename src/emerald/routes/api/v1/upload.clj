(ns emerald.routes.api.v1.upload
  (:require
   [compojure.api.sweet :refer :all]
   [ring.util.http-response :refer :all]
   [compojure.api.upload :as upload]
   [schema.core :as s]))

(defroutes* upload-routes
  (POST* "/upload" []
         :tags ["upload"]
         :summary "upload files here and retrieve thier url"
         :multipart-params [file :- upload/TempFileUpload]
         :middlewares [upload/wrap-multipart-params]
         (ok (dissoc file :tempfile))))
