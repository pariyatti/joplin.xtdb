(ns joplin.migrators.crux.20210306000000-second-migrator
  (:require [crux.api :as x]
            [joplin.crux.database :as d]
            [tick.alpha.api :as t]))

(def id "20210306000000-second-migrator")

(defn up [db]
  (let [node (d/get-node (:conf db))
        txs [[:crux.tx/put {:crux.db/id id
                            :schema/id id
                            :schema/created-at (t/now)}]]]
    (d/transact! node txs (format "Migrator '%s' failed to apply." id))))

(defn down [db]
  (let [node (d/get-node (:conf db))
        txs [[:crux.tx/delete id]]]
    (d/transact! node txs (format "Rollback '%s' failed to apply." id))))
