(ns joplin.crux.database-test
  (:require [clojure.test :refer :all]
            [clojure.java.io :as io]
            [crux.api :as x]
            [joplin.crux.database :as sut]
            [joplin.alias :refer [*load-config*]]
            [joplin.repl :as repl]
            [seeds.cx]))

(def config (*load-config* "joplin-cx.edn"))

(defn crux-conf []
  (-> config :databases :cx-dev :conf))

(defn destroy-node! []
  (reset! sut/crux-node nil)
  (sut/get-node (crux-conf)))

(defn destroy-fixture-file-copies! []
  (io/delete-file "test-resources/joplin/migrators/crux/20210306000000_second_migrator.clj"
                  :ignore-missing-file))

(defn copy-fixture-file! []
  (io/copy (io/file "test-resources/fixtures/20210306000000_second_migrator.clj")
           (io/file "test-resources/joplin/migrators/crux/20210306000000_second_migrator.clj")))

(defn with-empty-node [f]
  (destroy-node!)
  (f))

(defn with-fixture-files-cleanup [f]
  (destroy-fixture-file-copies!)
  (f)
  (destroy-fixture-file-copies!))

(use-fixtures :each with-empty-node with-fixture-files-cleanup)

(defn query-migrations []
  (x/q (x/db (sut/get-node (crux-conf)))
       '{:find [id]
         :where [[e :migrations/id id]]}))

(defn query-seeds []
  (x/q (x/db (sut/get-node (crux-conf)))
       '{:find [e]
         :where [[e :hamster/name n]]}))

(deftest adding-migrations
  (testing "adds one migration"
    (repl/migrate config :dev)
    (is (= 1 (-> (query-migrations) (count))))))

(deftest removing-migrations
  (testing "removes one migration from 'now'"
    (repl/migrate config :dev)
    (repl/rollback config :dev :cx-dev 1)
    (is (= 0 (-> (query-migrations) (count)))))

  (testing "removing a migration does NOT remove it from valid-time history"
    (repl/migrate config :dev)
    (let [between (java.util.Date.)
          _ (Thread/sleep 500)]
      (is (= 1 (-> (query-migrations) (count))))
      (repl/rollback config :dev :cx-dev 1)
      (is (= 1 (-> (x/q (x/db (sut/get-node (crux-conf)) between)
                        '{:find [e]
                          :where [[e :crux.db/id "20210302000000-test"]]})
                   (count)))))))

(deftest seeding
  (testing "adds seed data"
    (repl/migrate config :dev)
    (repl/seed config :dev)
    (is (= 3 (-> (query-seeds) (count))))))

(deftest resetting
  (testing "resetting the node removes and replaces old migrations"
    (destroy-node!)
    (repl/migrate config :dev)
    (repl/seed config :dev)
    (is (= 1 (-> (query-migrations) (count))))
    (repl/reset config :dev :cx-dev)
    (is (= 1 (-> (query-migrations) (count)))))

  (testing "resetting the node does NOT remove old seeds"
    (destroy-node!)
    (repl/migrate config :dev)
    (repl/seed config :dev)
    (is (= 3 (-> (query-seeds) (count))))
    (repl/reset config :dev :cx-dev)
    (is (= 6 (-> (query-seeds) (count))))))

(deftest pending
  (testing "pending knows how many migrations are left to run"
    (repl/migrate config :dev)
    (copy-fixture-file!)
    (is (= "Pending migrations (20210306000000-second-migrator)\n"
           (with-out-str (repl/pending config :dev :cx-dev))))))

;; TODO:
;; (repl/create)
;; - use transaction fns
;; - extract a submit+await+entity+exception fn
