# joplin.crux

```clojure
[joplin.crux "0.0.1-SNAPSHOT"]
```

## Usage - Clojure

You can use the `joplin.repl` namespace from within Clojure itself. In these examples:
- `config` is an example `joplin config` (EDN)
- `:prod` is an example _environment_
- `:cx-prod` is an example _database_

```clojure
(joplin.repl/migrate config :prod)
(joplin.repl/rollback config :prod :cx-prod 1)
;; or (joplin.repl/rollback config :prod :cx-prod 20151215114952-users)
(joplin.repl/seed config :prod)
(joplin.repl/reset config :prod :cx-prod)
(joplin.repl/pending config :prod :cx-prod)
```

## Usage - Command Line

You can run a lein alias from the command line if you [configure them in your `project.clj`, like in this example.](https://github.com/juxt/joplin/blob/master/example/project.clj#L15)

```shell
lein migrate prod
lein rollback prod cx-prod 1
# or lein rollback prod cx-prod 20151215114952-users
lein seed prod
lein reset prod cx-prod
lein pending prod cx-prod
```

## Generate Migrations

```clojure
(joplin.repl/create config :prod :cx-prod "add_users_schema")
```

or

```shell
lein create prod cx-prod add_users_schema
```

## Additional Documentation

- [Using Joplin (including a config example)](https://github.com/juxt/joplin#using-joplin)
- [Joplin Example Project](https://github.com/juxt/joplin/tree/master/example)
- [Joplin Example REPL Usage](https://github.com/juxt/joplin/blob/master/example/src/migrate.clj)
- [Joplin Example project.clj](https://github.com/juxt/joplin/blob/master/example/project.clj)

## TODO

- It was suggested by @jarohen that `joplin.crux` could use [Transaction Functions](https://opencrux.com/reference/21.02-1.15.0/transactions.html#transaction-functions) to ensure that all migrations executed at once share the same `tx-time`. This will definitely make the historical timeline for schema and data migrations cleaner, but it also requires a "meta schema change" of sorts, which is the Transaction Function itself. I'm a bit nervous making any assumptions about what sort of entities the consumers of `joplin.crux` want in their database, so I've left the direct (naive) implementation for now. This upgrade can always come later without impacting historical migrations.

- extract a submit+await+entity+exception fn ... all migrations need to be synchronous anyway.

## License

Copyright © 2021 Pariyatti

Distributed under either the MIT License or the Eclipse Public License (1.0 or later).
