(ns joplin.crux.database
  (:require [crux.api :as x]
            [joplin.core :as j] ;; clojure-lsp hates `:refer :all` for some reason
            [ragtime.protocols :refer [DataStore]]))

(def crux-node (atom nil))

(defn get-node [conf]
  (if @crux-node
    @crux-node
    (reset! crux-node (x/start-node conf))))

;; ============================================================================
;; Ragtime interface

(defrecord CruxDatabase [conf]
  DataStore

  (add-migration-id [this id]
    (when-let [node (get-node (:conf this))]
      (let [tx (x/submit-tx node [[:crux.tx/put {:crux.db/id id
                                                 :migrations/id id
                                                 :migrations/created-at (java.util.Date.)}]])
            _ (x/await-tx node tx)]
        (when-not (x/entity (x/db node) id)
          (throw (Exception. (format "Migration %s failed to apply." id)))))))

  (remove-migration-id [this id]
    (throw (Exception. "Not implemented - will we allow rollbacks?")))

  (applied-migration-ids [this]
    (when-let [node (get-node (:conf this))]
      (->> (x/q (x/db node)
                '{:find [id created-at]
                  :where [[e :migrations/id id]
                          [e :migrations/created-at created-at]]})
           (sort-by second)
           (map first)))))

(defn ->CruxDatabase [target]
  (CruxDatabase. (-> target :db :conf)))

;; ============================================================================
;; Joplin interface

(defmethod j/migrate-db :cx [target & args]
  (apply j/do-migrate (j/get-migrations (:migrator target))
         (->CruxDatabase target) args))

(defmethod j/rollback-db :cx [target amount-or-id & args]
  (apply j/do-rollback (j/get-migrations (:migrator target))
         (->CruxDatabase target) amount-or-id args))

(defmethod j/seed-db :cx [target & args]
  (apply j/do-seed-fn (j/get-migrations (:migrator target))
         (->CruxDatabase target) target args))

(defmethod j/pending-migrations :cx [target & _args]
  (j/do-pending-migrations (->CruxDatabase target)
                           (j/get-migrations (:migrator target))))

(defmethod j/create-migration :cx [target id & _args]
  (j/do-create-migration target id "joplin.crux.database"))
