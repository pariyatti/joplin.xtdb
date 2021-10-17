(ns joplin.xtdb.database
  (:require [xtdb.api :as xt]
            [joplin.core :refer :all] ;; clojure-lsp hates `:refer :all` for some reason
            [ragtime.protocols :refer [DataStore]]))

(def xtdb-node (atom nil))

(defn node-open? [node]
  (when node
    (try
      (xt/status node)
      (catch java.lang.IllegalStateException _e_
        false))))

(defn get-node
  "NOTE: `with-open` is not used within joplin.xtdb because it causes
         RocksDB errors on a shared node. However, it should be used
         everywhere else."
  [conf]
  (assert (isa? (class conf) clojure.lang.IPersistentMap))
  (if (node-open? @xtdb-node)
    @xtdb-node
    (reset! xtdb-node (xt/start-node conf))))

(defn transact! [node txns & [error-msg]]
  (let [tx (->> txns
                (xt/submit-tx node)
                (xt/await-tx node))]
    (when-not (xt/tx-committed? node tx)
      (throw (Exception. error-msg)))))

(defn close!
  "Unfortunately, clients must be responsible for closing
   the XTDB node if they want to use the same REPL with
   the same underlying disk stores (ex. RocksDB). This is
   because Joplin cannot close the XTDB node after every
   operation or the in-memory XTDB node won't work.

  This method exists as a convenience and living documentation.
  Prefer `(with-open (get-node conf))` wherever possible."
  []
  (when @xtdb-node
    (.close @xtdb-node))
  (reset! xtdb-node nil))

(defn query-migration-ids [node]
  (xt/q (xt/db node)
       '{:find [id created-at]
         :where [[e :migrations/id id]
                 [e :migrations/created-at created-at]]}))

;; ============================================================================
;; Ragtime interface

(defrecord XTDBDatabase [conf]
  DataStore

  (add-migration-id [this id]
    (let [node (get-node (:conf this))]
      (transact! node
                 [[::xt/put {:xt/id id
                                 :migrations/id id
                                 :migrations/created-at (java.util.Date.)}]]
                 (format "Migration '%s' failed to apply." id))
      (close!)))

  (remove-migration-id
    ;; "This function may seem naive, but it is actually the most honest approach.
    ;;  XTDB does not prevent users from using `delete` operations but it *does*
    ;;  force its users to acknowledge that `delete` is a command, not a mutation.
    ;;  Joplin is no exception to this rule. We can 'delete' a migration id just as
    ;;  we would any other data, but the history of the migration remains. Since the
    ;;  historical timeline is true, it is also correct."
    [this id]
    (let [node (get-node (:conf this))]
      (transact! node
                 [[::xt/delete id]]
                 (format "Rollback '%s' failed to apply." id))
      (close!)))

  (applied-migration-ids [this]
    (let [node (get-node (:conf this))
          applied (->> (query-migration-ids node)
                       (sort-by second)
                       (map first))]
      (close!)
      applied)))

(defn ->XTDBDatabase [target]
  (XTDBDatabase. (-> target :db :conf)))

;; ============================================================================
;; Joplin interface

(defmethod migrate-db :xtdb [target & args]
  (apply do-migrate (get-migrations (:migrator target))
         (->XTDBDatabase target) args))

(defmethod rollback-db :xtdb [target amount-or-id & args]
  (apply do-rollback (get-migrations (:migrator target))
         (->XTDBDatabase target) amount-or-id args))

(defmethod seed-db :xtdb [target & args]
  (apply do-seed-fn (get-migrations (:migrator target))
         (->XTDBDatabase target) target args))

(defmethod pending-migrations :xtdb [target & _args]
  (do-pending-migrations (->XTDBDatabase target)
                           (get-migrations (:migrator target))))

(defmethod create-migration :xtdb [target id & _args]
  (do-create-migration target id "joplin.xtdb.database"))
