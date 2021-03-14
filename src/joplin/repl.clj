(ns joplin.repl
  ;; force a require of `joplin.crux.database` since this
  ;; otherwise happens magically in the original `joplin.crux`
  ;; ns but with no awareness of :crux multimethods:
  (:require [joplin.crux.database]))
