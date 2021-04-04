# joplin.crux

Migrate and seed Crux data.

[![Clojars Project](https://img.shields.io/clojars/v/org.pariyatti/joplin.crux.svg)](https://clojars.org/org.pariyatti/joplin.crux)

## Install

lein / boot:
```clojure
[org.pariyatti/joplin.crux "0.0.2"]
```

CLI / deps.edn:
```clojure
org.pariyatti/joplin.crux {:mvn/version "0.0.2"}
```

## Usage - Clojure

You can use the `joplin.repl` namespace from within Clojure itself. In these examples:
- `config` is an example Joplin Config (EDN)
- `:prod` is an example _environment_
- `:crux-prod` is an example _database_

```clojure
(joplin.repl/migrate config :prod)
(joplin.repl/rollback config :prod :crux-prod 1)
;; or (joplin.repl/rollback config :prod :crux-prod 20151215114952-users)
(joplin.repl/seed config :prod)
(joplin.repl/reset config :prod :crux-prod)
(joplin.repl/pending config :prod :crux-prod)
```

## Usage - Command Line

You can run a lein alias from the command line if you configure them in your `project.clj`, like so:

```clojure
:aliases {"migrate"  ["run" "-m" "joplin.crux.alias/migrate"  "joplin/config.edn"]
          "seed"     ["run" "-m" "joplin.crux.alias/seed"     "joplin/config.edn"]
          "rollback" ["run" "-m" "joplin.crux.alias/rollback" "joplin/config.edn"]
          "reset"    ["run" "-m" "joplin.crux.alias/reset"    "joplin/config.edn"]
          "pending"  ["run" "-m" "joplin.crux.alias/pending"  "joplin/config.edn"]
          "create"   ["run" "-m" "joplin.crux.alias/create"   "joplin/config.edn"]}
```

Then you can run Joplin commands from the command line:

```shell
lein migrate prod
lein rollback prod crux-prod 1
# or lein rollback prod crux-prod 20151215114952-users
lein seed prod
lein reset prod crux-prod
lein pending prod crux-prod
```

**NOTE:** You must refer to the `joplin.crux.alias` shim in your aliases, unlike the [default alias configuration seen in the joplin.core example.](https://github.com/juxt/joplin/blob/master/example/project.clj#L15). Because `joplin.crux` is a plugin, `joplin.alias` doesn't know about it out of the box. This is arguably a bug in Joplin.

## Generate Migrations

```clojure
(joplin.repl/create config :prod :crux-prod "add_users_schema")
```

or

```shell
lein create prod crux-prod add_users_schema
```

## Additional Documentation

- [Using Joplin (including a config example)](https://github.com/juxt/joplin#using-joplin)
- [Joplin Example Project](https://github.com/juxt/joplin/tree/master/example)
- [Joplin Example REPL Usage](https://github.com/juxt/joplin/blob/master/example/src/migrate.clj)
- [Joplin Example project.clj](https://github.com/juxt/joplin/blob/master/example/project.clj)

## TODO

- It was suggested by @jarohen that `joplin.crux` could use [Transaction Functions](https://opencrux.com/reference/21.02-1.15.0/transactions.html#transaction-functions) to ensure that all migrations executed at once share the same `tx-time`. This will definitely make the historical timeline for schema and data migrations cleaner, but it also requires a "meta schema change" of sorts, which is the Transaction Function itself. I'm a bit nervous making any assumptions about what sort of entities the consumers of `joplin.crux` want in their database, so I've left the direct (naive) implementation for now. This upgrade can always come later without impacting historical migrations.

## Developing

Run tests:

```shell
lein test
```

## Deploying to Clojars

### One-time Setup

1. Get admin permissions to the [org.pariyatti Clojars group](https://clojars.org/org.pariyatti). This group is already verified against [pariyatti.org](https://pariyatti.org).
2. Set up GPG:

```shell
brew install gnupg2
echo "GPG_TTY=$(tty)\nexport GPG_TTY" >> ~/.zshrc # or ~/.bash_profile
gpg --gen-key
# save your passphrase in your password manager
gpg --list-keys
gpg --fingerprint 1A1A11A11AA11A111A1AAA1A11AA1111A11A1111
gpg --send-keys   1A1A11A11AA11A111A1AAA1A11AA1111A11A1111
# save your revocation certificate in your password manager:
cat ~/.gnupg/openpgp-revocs.d/1A1A11A11AA11A111A1AAA1A11AA1111A11A1111.rev
```

3. Create a [Clojars Deploy Token](https://clojars.org/tokens), which is now mandatory. Save it in your password manager.
4. Create `~/.lein/credentials.clj` (as described [here](https://tech.toryanderson.com/2020/07/21/deploying-to-clojars-with-the-new-tokens/)):

```clojure
{#"clojars"
 {:username "my-web-username"
  :password "CLOJARS_5a5a5aa5a555555555a55aa..."}}
```

5. Encrypt it:

```shell
gpg --default-recipient-self -e ~/.lein/credentials.clj > ~/.lein/credentials.clj.gpg
```

6. Try it out:

```shell
lein deploy clojars
```

### Deployment Checklist

1. Bump (drop "SNAPSHOT") and commit a new SemVer in `project.clj`.
2. Deploy:

```shell
lein deploy clojars
```

3. Bump and commit `"SemVer+1-SNAPSHOT"`

## License

Copyright © 2021 Pariyatti

Distributed under either the MIT License or the Eclipse Public License (1.0 or later).
