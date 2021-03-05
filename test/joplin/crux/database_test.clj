(ns joplin.crux.database-test
  (:require [clojure.test :refer :all]
            [crux.api :as x]
            [joplin.crux.database :as sut]
            [joplin.alias :refer [*load-config*]]
            [joplin.repl :as repl]))

(def config (*load-config* "joplin-cx.edn"))

(deftest adding-migrations
  (testing "adds one migration"
    (repl/migrate config :dev)
    (is (= 1 (-> (x/q (x/db (sut/get-node config))
                      '{:find [id]
                        :where [[e :migrations/id id]]})
                 (count))))))
