(ns joplin.migrators.crux.20210306000000-second-migrator
  (:require [crux.api :as x]
            [joplin.crux.database :as d]
            [tick.alpha.api :as t]))

(def id "20210306000000-second-migrator")

(defn up [db]
  (let [node (d/get-node (:conf db))
        tx (x/submit-tx node [[:crux.tx/put {:crux.db/id id
                                             :schema/id id
                                             :schema/created-at (t/now)}]])
        _ (x/await-tx node tx)]
    ;; it might be wise for real migrators to assert the migration was
    ;; successfully saved like this:
    (when-not (x/entity (x/db node) id)
      (throw (Exception. (format "Migrator '%s' failed to apply." id))))))

(defn down [db]
  (let [node (d/get-node (:conf db))
        tx (x/submit-tx node [[:crux.tx/delete id]])
        _ (x/await-tx node tx)]))
