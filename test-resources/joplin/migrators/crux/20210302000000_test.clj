(ns joplin.migrators.crux.20210302000000-test
  (:require [crux.api :as x]
            [joplin.crux.database :as d]
            [tick.alpha.api :as t]))

;; (ns migrators.cass.20140717174605-users
;;   )

(defn up [db]
  (let [conn (d/get-connection (:conf db))]
    (x/submit-tx conn [[:crux.tx/put {:crux.db/id "20210302000000-test"
                                      :schema/id "20210302000000-test"
                                      :schema/created-at (t/now)}]])))

(defn down [db]
  (let [conn (d/get-connection (:conf db))]
    (x/submit-tx conn [[:crux.tx/delete "20210302000000-test"]])))
