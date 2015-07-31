(ns emerald.routes.home
  (:require [emerald.layout :as layout]
            [compojure.core :refer [defroutes GET POST]]
            [ring.util.http-response :refer [ok]]
            [clojure.java.io :as io]
            [emerald.models.user :as users]
            [emerald.models.application :as app]
            [clauth.token :as ct]
            [clauth.endpoints :refer [authorization-handler]]))

(defn home-page []
  (layout/render
    "home.html" {:docs (-> "docs/docs.md" io/resource slurp)}))

(defn login-page [& user]
  (layout/render "login.html" (first user)))

(defn app-page []
  (layout/render "apps.html"))

(defn app-creation-page []
  (layout/render "applications/new.html"))

(defn try-login [slug]
  (let [user (users/login! slug)]
    (if (:error user)
      (login-page user)
      (println user)
    )))

(defroutes home-routes
  (GET "/" [] (home-page))
  (GET "/authorize" req ((authorization-handler) req))
  (GET "/login" [] (login-page))
  (POST "/login" {params :params} (try-login params))
  (GET "/apps" [] (app-page))
  (GET "/apps/create" [] (app-creation-page))
  )
