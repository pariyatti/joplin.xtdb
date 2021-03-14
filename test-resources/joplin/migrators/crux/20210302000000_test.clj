(ns joplin.migrators.crux.20210302000000-test
  (:require [crux.api :as x]
            [joplin.crux.database :as d]
            [tick.alpha.api :as t]))

(defn up [db]
  (let [id "20210302000000-test"
        node (d/get-node (:conf db))
        txs [[:crux.tx/put {:crux.db/id id
                            :schema/id "20210302000000-test"
                            :schema/created-at (t/now)}]]]
    (d/transact! node txs (format "Migrator '%s' failed to apply." id))))

(defn down [db]
  (let [id "20210302000000-test"
        node (d/get-node (:conf db))
        txs [[:crux.tx/delete id]]]
    (d/transact! node txs (format "Rollback '%s' failed to apply." id))))
