(ns emerald.db.core
  (:refer-clojure :exclude [update])
  (:use [korma.core]
        [emerald.util.core]
        [emerald.db.helpers])
  (:require [korma.db :refer [defdb]]
            [korma.core :refer :all]
            [emerald.env :refer [env]]))

(defdb db (env :dbspec))

(declare
  adtags
  adtag-templates
  placements
  publishers
  creatives
  target-creatives
  targets
  clients
  channels
  accounts
  industries
  campaigns
  creatives
  divisions
  accounts
  geo-profiles
  media
  applications
  users
  client-pins
  division-pins
  account-pins
  campaign-pins
  user-account-permissions
  user-client-permissions
  user-division-permissions)

(defn prepare-fns [m]
  (-> (to-dash m)
      (handle-enum)))

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
  (prepare prepare-fns)
  (transform camel-case)
  (belongs-to channels {:fk :channel_id})
  (has-many divisions {:fk :client_id})
  (has-many client-pins {:fk :client_id})
  (has-many geo-profiles {:fk :client_id})
  (has-many publishers {:fk :client_id})
  (table :mixpo.clients :client))

(defentity divisions
  (prepare prepare-fns)
  (transform camel-case)
  (belongs-to clients {:fk :client_id})
  (has-many accounts {:fk :division_id})
  (has-many division-pins {:fk :division_id})
  (table :mixpo.divisions :divisions))

(defentity accounts
  (prepare prepare-fns)
  (transform camel-case)
  (has-many campaigns {:fk :account_id})
  (has-many account-pins {:fk :account_id})
  (belongs-to divisions {:fk :division_id})
  (belongs-to industries {:fk :industry_id})
  (table :mixpo.accounts :account))

(defentity campaigns
  (prepare prepare-fns)
  (transform camel-case)
  (has-many campaign-pins {:fk :campaign_id})
  (has-many placements {:fk :campaign_id})
  (belongs-to accounts {:fk :account_id})
  (table :mixpo.campaigns :campaigns))

(defentity placements
  (prepare prepare-fns)
  (transform camel-case)
  (belongs-to campaigns {:fk :campaign_id})
  (belongs-to publishers {:fk :publisher_id})
  (has-many targets {:fk :placement_id})
  (table :mixpo.placements :placement))

(defentity creatives
  (prepare prepare-fns)
  (transform camel-case)
  (table :mixpo.creatives :creative))

(defentity industries
  (prepare prepare-fns)
  (transform camel-case)
  (table :mixpo.industries :industries))

(defentity channels
  (prepare prepare-fns)
  (transform camel-case)
  (table :mixpo.channels :channel))

(defentity targets
  (belongs-to placements {:fk :placement_id})
  (many-to-many creatives :mixpo.target_creatives {:lfk :src_target_id :rfk :creative_id})
  (table :mixpo.targets :targets))

(defentity target-creatives
  (belongs-to creatives {:fk :creative_id})
  (table :mixpo.target_creatives :target_creatives))

(defentity publishers
  (prepare prepare-fns)
  (transform camel-case)
  (belongs-to clients {:fk :client_id})
  (table :mixpo.publishers :publisher))

(defentity geo-profiles
  (prepare prepare-fns)
  (transform camel-case)
  (belongs-to clients {:fk :client_id})
  (table :mixpo.geo_profiles :geo_profiles))

(defentity applications
  (prepare prepare-fns)
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

(defentity adtag-templates
   (transform camel-case)
   (table :mixpo.ad_tag_templates))

(defentity adtags
  (transform camel-case)
  (belongs-to adtag-templates {:fk :ad_tags.ad_tag_template_id})
  (table :mixpo.ad_tags :ad_tags))

(defentity media
  (transform camel-case)
  (table :mixpo.media))

(defentity creative-media
  (transform camel-case)
  (table :mixpo.creative_media)
  (belongs-to media {:fk :media_id})
  (belongs-to creatives {:fk :creative_id}))
