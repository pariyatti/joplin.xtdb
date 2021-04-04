(defproject org.pariyatti/joplin.crux "0.0.3-SNAPSHOT"
  :description "Crux support for Joplin"
  :url "http://github.com/pariyatti/joplin.crux"
  :scm {:name "git"
        :url "https://github.com/pariyatti/joplin.crux"}
  :license {:name "The MIT License"
            :url "http://opensource.org/licenses/MIT"}
  :dependencies [[org.clojure/clojure "1.10.3"]
                 [joplin.core "0.3.11"]
                 [juxt/crux-core "21.04-1.16.0-beta"]
                 [juxt/crux-rocksdb "21.04-1.16.0-beta"]
                 [juxt/crux-lucene "21.04-1.16.0-alpha"]]

  :profiles {:dev {:resource-paths ["test-resources"]
                   :dependencies [[tick "0.4.30-alpha"]
                                  [babashka/fs "0.0.1"]]}})
