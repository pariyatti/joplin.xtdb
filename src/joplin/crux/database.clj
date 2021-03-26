(ns joplin.crux.database
  (:require [crux.api :as x]
            [joplin.core :refer :all] ;; clojure-lsp hates `:refer :all` for some reason
            [ragtime.protocols :refer [DataStore]]))

(def crux-node (atom nil))

(defn get-node [conf]
  (if @crux-node
    @crux-node
    (reset! crux-node (x/start-node conf))))

(defn transact! [node txns & [error-msg]]
  (let [tx (->> txns
                (x/submit-tx node)
                (x/await-tx node))]
    (when-not (x/tx-committed? node tx)
      (throw (Exception. error-msg)))))

(defn close!
  "Unfortunately, clients must be responsible for closing
   the Crux node if they want to use the same REPL with
   the same underlying disk stores (ex. RocksDB). This is
   because Joplin cannot close the Crux node after every
   operation or the in-memory Crux node won't work.

  This method exists as a convenience and living documentation.
  Prefer `(with-open (get-node conf))` wherever possible."
  []
  (when @crux-node
   (.close @crux-node)))

;; ============================================================================
;; Ragtime interface

(defrecord CruxDatabase [conf]
  DataStore

  (add-migration-id [this id]
    (transact! (get-node (:conf this))
               [[:crux.tx/put {:crux.db/id id
                               :migrations/id id
                               :migrations/created-at (java.util.Date.)}]]
               (format "Migration '%s' failed to apply." id)))

  (remove-migration-id
    ;; "This function may seem naive, but it is actually the most honest approach.
    ;;  Crux does not prevent users from using `delete` operations but it *does*
    ;;  force its users to acknowledge that `delete` is a command, not a mutation.
    ;;  Joplin is no exception to this rule. We can 'delete' a migration id just as
    ;;  we would any other data, but the history of the migration remains. Since the
    ;;  historical timeline is true, it is also correct."
    [this id]
    (transact! (get-node (:conf this))
               [[:crux.tx/delete id]]
               (format "Rollback '%s' failed to apply." id)))

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

(defmethod migrate-db :crux [target & args]
  (apply do-migrate (get-migrations (:migrator target))
         (->CruxDatabase target) args))

(defmethod rollback-db :crux [target amount-or-id & args]
  (apply do-rollback (get-migrations (:migrator target))
         (->CruxDatabase target) amount-or-id args))

(defmethod seed-db :crux [target & args]
  (apply do-seed-fn (get-migrations (:migrator target))
         (->CruxDatabase target) target args))

(defmethod pending-migrations :crux [target & _args]
  (do-pending-migrations (->CruxDatabase target)
                           (get-migrations (:migrator target))))

(defmethod create-migration :crux [target id & _args]
  (do-create-migration target id "joplin.crux.database"))
