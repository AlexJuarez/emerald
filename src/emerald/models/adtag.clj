(ns emerald.models.adtag
  (:refer-clojure :exclude [update get])
  (:use
   [korma.db :only (transaction)]
   [korma.core]
   [emerald.db.core]))

(defn all []
  (select adtags
          (fields :ad_tags.id :ad_tags.name :ad_tags.player_code :ad_tags.version_suffix :ad_tags.is_in_stream :ad_tags.attributes :ad_tag_templates.template)
          (join adtag-templates)))
