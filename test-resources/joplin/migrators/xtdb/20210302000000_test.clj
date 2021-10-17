(ns joplin.migrators.xtdb.20210302000000-test
  (:require [xtdb.api :as xt]
            [joplin.xtdb.database :as d]
            [tick.alpha.api :as t]))

;; NOTE: remember not to use the same id as the name of this
;;       ns or it will collide with joplin's migrator ids.
(def id "test-schema-id")

(defn up [db]
  ;; NOTE: in a real migration, use `with-open`. The joplin.xtdb test suite
  ;;       seems to fail when using `with-open` because we use a shared node.
  (let [node (d/get-node (:conf db))]
    (let [txs [[::xt/put {:xt/id id
                              :schema/id id
                              :schema/created-at (t/now)}]]]
      (d/transact! node txs (format "Migrator '%s' failed to apply." id))))
  (d/close!))

(defn down [db]
  (let [node (d/get-node (:conf db))]
    (let [txs [[::xt/delete id]]]
      (d/transact! node txs (format "Rollback '%s' failed to apply." id))))
  (d/close!))
