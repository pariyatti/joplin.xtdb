(defproject org.pariyatti/joplin.xtdb "0.0.5-SNAPSHOT"
  :description "XTDB support for Joplin"
  :url "http://github.com/pariyatti/joplin.xtdb"
  :scm {:name "git"
        :url "https://github.com/pariyatti/joplin.xtdb"}
  :license {:name "The MIT License"
            :url "http://opensource.org/licenses/MIT"}

  :dependencies [[org.clojure/clojure "1.10.3"]
                 [joplin.core         "0.3.11"]
                 [com.xtdb/xtdb-core    "1.19.0"]
                 [com.xtdb/xtdb-rocksdb "1.19.0"]
                 [com.xtdb/xtdb-lucene  "1.19.0"]]

  :profiles {:dev {:resource-paths ["test-resources"]
                   :dependencies [[tick "0.4.30-alpha"]
                                  [babashka/fs "0.0.1"]]}})
