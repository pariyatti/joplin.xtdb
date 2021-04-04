(ns joplin.crux.database-test
  (:require [clojure.test :refer :all]
            [clojure.java.io :as io]
            [babashka.fs :as fs]
            [crux.api :as x]
            [joplin.crux.database :as sut]
            [joplin.alias :refer [*load-config*]]
            [joplin.repl :as repl]
            [seeds.crux]))

;; WARNING: Joplin's ability to parse and eval migration and seed
;;          files seems surprisingly fragile, and running these
;;          tests from a REPL may cause inconsistent compiler
;;          failures. If the tests are working from `lein test`
;;          but fail at the REPL, compile these files and try again:
;;
;;          test-resources/joplin/migrators/crux/20210302000000_test.clj
;;          test/seeds/crux.clj

(def config (*load-config* "joplin-crux.edn"))

(defn crux-conf []
  (-> config :databases :crux-dev :conf))

(defn rm-rf
  "Recursively delete a directory."
  [^java.io.File file & [silently]]
  (when (.isDirectory file)
    (doseq [file-in-dir (.listFiles file)]
      (rm-rf file-in-dir)))
  (io/delete-file file silently))

(defn destroy-data-dir! []
  (rm-rf (io/file "data/") true))

(defn destroy-node! []
  (sut/close!)
  (destroy-data-dir!)
  (sut/get-node (crux-conf)))

(defn destroy-fixture-file-copies! []
  (fs/delete-if-exists "test-resources/joplin/migrators/crux/20210306000000_second_migrator.clj")
  (some->> (fs/glob "test-resources/joplin/migrators/crux/"
                    "**_wakkawakka.clj")
           first
           fs/delete-if-exists))

(defn copy-fixture-file! []
  (io/copy (io/file "test-resources/fixtures/20210306000000_second_migrator.clj")
           (io/file "test-resources/joplin/migrators/crux/20210306000000_second_migrator.clj")))

(defn with-empty-node [t]
  (destroy-node!)
  (t))

(defn with-empty-data-dir [t]
  (destroy-data-dir!)
  (t))

(defn with-fixture-files-cleanup [t]
  (destroy-fixture-file-copies!)
  (t)
  (destroy-fixture-file-copies!))

(use-fixtures :each
  with-empty-data-dir
  with-empty-node
  with-fixture-files-cleanup)

(defn query-migrations []
  (x/q (x/db (sut/get-node (crux-conf)))
       '{:find [id]
         :where [[e :migrations/id id]]}))

(defn migration-count []
  (-> (query-migrations) (count)))

(defn query-seeds []
  (x/q (x/db (sut/get-node (crux-conf)))
       '{:find [e]
         :where [[e :hamster/name n]]}))

(defn seed-count []
  (-> (query-seeds) (count)))

(deftest adding-migrations
  (testing "adds one migration"
    (repl/migrate config :dev)
    (is (= 1 (-> (query-migrations) (count))))))

(deftest removing-migrations
  (testing "removes one migration from 'now'"
    (repl/migrate config :dev)
    (let [n (migration-count)]
      (repl/rollback config :dev :crux-dev 1)
      (is (= (- n 1) (migration-count)))))

  (testing "removing a migration does NOT remove it from valid-time history"
    (repl/migrate config :dev)
    (let [between (java.util.Date.)
          _ (Thread/sleep 500)]
      (is (= 1 (migration-count)))
      (repl/rollback config :dev :crux-dev 1)
      (is (= 1 (-> (x/q (x/db (sut/get-node (crux-conf)) between)
                        '{:find [e]
                          :where [[e :crux.db/id "20210302000000-test"]]})
                   (count)))))))

(deftest seeding
  (testing "adds seed data"
    (repl/migrate config :dev)
    (repl/seed config :dev)
    (is (= 3 (seed-count)))))

(deftest resetting
  (testing "resetting the node removes and replaces old migrations"
    (destroy-node!)
    (repl/migrate config :dev)
    (repl/seed config :dev)
    (is (= 1 (migration-count)))
    (repl/reset config :dev :crux-dev)
    (is (= 1 (migration-count))))

  (testing "resetting the node does NOT remove old seeds"
    (destroy-node!)
    (repl/migrate config :dev)
    (repl/seed config :dev)
    (is (= 3 (seed-count)))
    (repl/reset config :dev :crux-dev)
    (is (= 6 (seed-count)))))

(deftest pending
  (testing "pending knows how many migrations are left to run"
    (repl/migrate config :dev)
    (copy-fixture-file!)
    (is (= "Pending migrations (20210306000000-second-migrator)\n"
           (with-out-str (repl/pending config :dev :crux-dev))))))

(deftest creating-migrations
  (testing "creates an empty migration"
    (repl/create config :dev :crux-dev "wakkawakka")
    (is (= 1 (->> (fs/glob "test-resources/joplin/migrators/crux/"
                           "**_wakkawakka.clj")
                  (map str)
                  count)))))

(deftest closed-node
  (testing "knows when the node is closed even with a dirty reference"
    (let [node (sut/get-node (crux-conf))]
      (is (sut/node-open? node))
      (.close node)
      (is (not (nil? @sut/crux-node)))
      (is (not (sut/node-open? node)))))

  (testing "explicit close cleans up the reference"
    (let [node (sut/get-node (crux-conf))]
      (is (sut/node-open? node))
      (sut/close!)
      (is (nil? @sut/crux-node))
      (is (not (sut/node-open? node)))))

  (testing "get-node always returns a node"
    (let [node (sut/get-node (crux-conf))]
      (is (sut/node-open? node))
      (.close node)
      (let [node (sut/get-node (crux-conf))]
        (is (sut/node-open? node))))))
