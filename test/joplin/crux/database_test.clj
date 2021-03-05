(ns joplin.crux.database-test
  (:require [clojure.test :refer :all]
            [joplin.crux.database :as sut]
            [joplin.alias :refer [*load-config*]]
            [joplin.repl :as repl]))

(def config (*load-config* "joplin-cx.edn"))

(deftest adding-migrations
  (testing "adds one migration"
    (repl/migrate config :dev)))
