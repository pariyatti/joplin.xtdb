(ns joplin.xtdb.alias
  (:require [joplin.alias]
            [joplin.repl]))

(def migrate joplin.alias/migrate)
(def seed joplin.alias/seed)
(def rollback joplin.alias/rollback)
(def reset joplin.alias/reset)
(def pending joplin.alias/pending)
(def create joplin.alias/create)

(ns joplin.repl
  (:require [joplin.xtdb.database]))
