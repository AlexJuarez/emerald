(ns emerald.routes.home
  (:require [emerald.layout :as layout]
            [compojure.core :refer [defroutes GET POST context]]
            [emerald.util.session :as session]
            [clojure.java.io :as io]
            [emerald.models.user :as users]
            [emerald.oauth.token :as token]
            [emerald.models.application :as app]
            [ring.util.response :as resp]
            [clauth.endpoints :refer [authorization-handler]]
            [bouncer.core :as b]
            [bouncer.validators :as v]))

(defn home-page []
  (layout/render
    "home.html" {:docs (-> "docs/docs.md" io/resource slurp)}))

(defn login-page [& user]
  (layout/render "login.html" (first user)))

(defn apps-page [req]
  (let [applications (app/all-for-user (get-in req [:session :user :id]))]
    (layout/render "apps.html" {:applications applications})))

(defn app-page [id req]
  (let [application (app/get id (get-in req [:session :user :id]))
        token (token/grant-for-user (session/get-in [:session :user :id]))]
    (layout/render "applications/test.html" (assoc application :accessToken token))))

(defn app-creation-page
  ([]
   (layout/render "applications/new.html"))
  ([{:keys [params] :as req}]
   (let [errors (first (b/validate params
                            :name v/required
                            :website v/required
                            :callbackUrl v/required))]
     (if (nil? errors)
       (let [application (app/add! params (get-in req [:session :user :id]))]
         (resp/redirect "/apps"))
       (layout/render "applications/new.html" (merge params {:errors errors}))))))

(defn try-login [{:keys [params] :as req}]
  (let [user (users/login! params)]
    (if (:error user)
      (login-page user)
      (->
       (resp/redirect "/apps")
       (assoc-in [:session :user] user))
    )))

(defn as-uuid [uuid]
  (java.util.UUID/fromString uuid))

(defroutes home-routes
  (GET "/" [] (home-page))
  (GET "/authorize" req ((authorization-handler) req))
  (GET "/login" [] (login-page))
  (POST "/login" req (try-login req))
  (context "/apps" []
           (GET "/" req (apps-page req))
           (GET "/:id/oauth/test" [id :<< as-uuid :as req] (app-page id req))
           (GET "/create" [] (app-creation-page))
           (POST "/create" req (app-creation-page req))))
