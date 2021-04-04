(ns seeds.crux
  (:require [crux.api :as x]
            [joplin.crux.database :as d]))

(defn run [target & args]
  ;; NOTE: in a real migration, use `with-open`. The joplin.crux test suite
  ;;       seems to fail when using `with-open` because we use a shared node.
  (when-let [node (d/get-node (-> target :db :conf))]
    (let [txs [[:crux.tx/put {:crux.db/id (java.util.UUID/randomUUID)
                              :hamster/name "Farley Moat"
                              :hamster/age 12}]
               [:crux.tx/put {:crux.db/id (java.util.UUID/randomUUID)
                              :hamster/name "Barley Goat"
                              :hamster/age 3}]
               [:crux.tx/put {:crux.db/id (java.util.UUID/randomUUID)
                              :hamster/name "Gnarly Rote"
                              :hamster/age 99}]]]
      (d/transact! node txs (format "Seed '%s' failed to apply." (ns-name *ns*)))))
  (d/close!))
