(ns emerald.db.core
  (:use [korma.core]
        [emerald.util.core]
        [emerald.db.helpers])
  (:require [korma.db :refer [defdb]]
            [korma.core :refer :all]
            [emerald.env :refer [env]]))

(defdb db (env :dbspec))

(declare
  adtags
  placements
  publishers
  creatives
  clients
  channels
  accounts
  industries
  campaigns
  creatives
  divisions
  accounts
  geo-profiles
  applications
  users
  client-pins
  division-pins
  account-pins
  campaign-pins
  user-account-permissions
  user-client-permissions
  user-division-permissions)

(defentity user-account-permissions
  (table :mixpo.user_account_permissions))

(defentity user-division-permissions
  (table :mixpo.user_division_permissions))

(defentity user-client-permissions
  (table :mixpo.user_client_permissions))

(defentity users
  (belongs-to clients {:fk :client_id})
  (table :mixpo.users :users))

(defentity clients
  (prepare to-dash)
  (transform camel-case)
  (belongs-to channels {:fk :channel_id})
  (has-many geo-profiles {:fk :client_id})
  (has-many publishers {:fk :client_id})
  (table :mixpo.clients :client))

(defentity accounts
  (prepare to-dash)
  (transform camel-case)
  (belongs-to divisions {:fk :division_id})
  (belongs-to industries {:fk :industry_id})
  (table :mixpo.accounts :account))

(defentity industries
  (prepare to-dash)
  (transform camel-case)
  (table :mixpo.industries :industries))

(defentity campaigns
  (prepare to-dash)
  (transform camel-case)
  (belongs-to accounts {:fk :account_id})
  (table :mixpo.campaigns :campaigns))

(defentity divisions
  (prepare to-dash)
  (transform camel-case)
  (table :mixpo.divisions :divisions))

(defentity channels
  (prepare to-dash)
  (transform camel-case)
  (table :mixpo.channels :channel))

(defentity placements
  (prepare to-dash)
  (transform camel-case)
  (belongs-to publishers {:fk :publisher_id})
  (many-to-many creatives :mixpo.targets {:lfk :placement_id :rfk :creative_id})
  (table :mixpo.placements :placement))

(defentity publishers
  (prepare to-dash)
  (transform camel-case)
  (belongs-to clients {:fk :client_id})
  (table :mixpo.publishers :publisher))

(defentity creatives
  (prepare to-dash)
  (transform camel-case)
  (table :mixpo.creatives :creative))

(defentity geo-profiles
  (prepare to-dash)
  (transform camel-case)
  (belongs-to clients {:fk :client_id})
  (table :mixpo.geo_profiles :geo_profiles))

(defentity applications
  (prepare to-dash)
  (transform camel-case)
  (belongs-to users {:fk :user_id})
  (table :mixpo.applications :applications))

(defentity client-pins
  (transform camel-case)
  (table :mixpo.user_client_pins))

(defentity division-pins
  (transform camel-case)
  (table :mixpo.user_division_pins))

(defentity account-pins
  (transform camel-case)
  (table :mixpo.user_account_pins))

(defentity campaign-pins
  (transform camel-case)
  (table :mixpo.user_campaign_pins))

(defentity adtags
  (transform camel-case)
  (table :mixpo.adtags))
