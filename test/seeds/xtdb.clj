(ns seeds.xtdb
  (:require [xtdb.api :as xt]
            [joplin.xtdb.database :as d]))

(defn run [target & args]
  ;; NOTE: in a real migration, use `with-open`. The joplin.xtdb test suite
  ;;       seems to fail when using `with-open` because we use a shared node.
  (when-let [node (d/get-node (-> target :db :conf))]
    (let [txs [[::xt/put {:xt/id (java.util.UUID/randomUUID)
                              :hamster/name "Farley Moat"
                              :hamster/age 12}]
               [::xt/put {:xt/id (java.util.UUID/randomUUID)
                              :hamster/name "Barley Goat"
                              :hamster/age 3}]
               [::xt/put {:xt/id (java.util.UUID/randomUUID)
                              :hamster/name "Gnarly Rote"
                              :hamster/age 99}]]]
      (d/transact! node txs (format "Seed '%s' failed to apply." (ns-name *ns*)))))
  (d/close!))
