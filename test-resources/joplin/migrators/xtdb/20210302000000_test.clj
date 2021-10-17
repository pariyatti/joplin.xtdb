(ns joplin.migrators.crux.20210302000000-test
  (:require [crux.api :as x]
            [joplin.crux.database :as d]
            [tick.alpha.api :as t]))

;; NOTE: remember not to use the same id as the name of this
;;       ns or it will collide with joplin's migrator ids.
(def id "test-schema-id")

(defn up [db]
  ;; NOTE: in a real migration, use `with-open`. The joplin.crux test suite
  ;;       seems to fail when using `with-open` because we use a shared node.
  (let [node (d/get-node (:conf db))]
    (let [txs [[:crux.tx/put {:crux.db/id id
                              :schema/id id
                              :schema/created-at (t/now)}]]]
      (d/transact! node txs (format "Migrator '%s' failed to apply." id))))
  (d/close!))

(defn down [db]
  (let [node (d/get-node (:conf db))]
    (let [txs [[:crux.tx/delete id]]]
      (d/transact! node txs (format "Rollback '%s' failed to apply." id))))
  (d/close!))
