(ns joplin.migrators.xtdb.20210317020346-add-image-artefacts
  (:require [xtdb.api :as xt]
            [joplin.xtdb.database :as d]
            [tick.alpha.api :as t]))

;; NOTE: this file is really more of a sample and should be moved into
;;       the joplin.xtdb tests -sd

(defn mk-tx-fn [node]
  (d/transact! node
               [[::xt/put {:xt/id :add-owner
	                 :xt/fn '(fn [ctx]
	                                (let [db (xtdb.api/db ctx)
                                        ids (xtdb.api/q db
                                                        '{:find  [e]
                                                          :where [[e :type "image-artefact"]]})
                                        entities (map #(xtdb.api/entity db (first %)) ids)]
                                    (vec (map (fn [entity]
                                                [::xt/put (assoc entity :owner nil)])
                                              entities))
	                                  ))}]]
               (format "Migration '%s' failed to apply." (ns-name *ns*))))

(defn run-tx-fn [node]
  (d/transact! node
               [[::xt/fn
	               :add-owner]]
               (format "Migration '%s' failed to apply." (ns-name *ns*))))

(defn up [db]
  (let [node (d/get-node (:conf db))]
    (mk-tx-fn node)
    (run-tx-fn node))
  ;; (let [node (d/get-node (:conf db))
  ;;       txs [[::xt/put {:xt/id id
  ;;                           :schema/id id
  ;;                           :schema/created-at (t/now)}]]]
  ;;   (d/transact! node txs (format "Migrator '%s' failed to apply." id)))
  )

;; TODO: close node when finished

(defn down [db]
  ;; (let [node (d/get-node (:conf db))
  ;;       txs [[::xt/delete id]]]
  ;;   (d/transact! node txs (format "Rollback '%s' failed to apply." id)))
  )
