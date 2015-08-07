#### Usage

Download and run `lein install` from this project's root directory.

In your project.clj, add `[pure-clojure-sha-1 "0.1.0"]` to the :dependencies vector.

When using, `:require [joshua-g.sha-1 :as s]` within your `ns` declaration, or `(require '[joshua-g.sha-1 :as s])` from the REPL.

##### Examples

```
user> (require '[joshua-g.sha-1 :as s])
nil

user> (->> (map int "The quick brown fox jumps over the lazy dog")
           s/sha-1
           (map (partial format "%02x"))
           (apply str))
"2fd4e1c67a2d28fced849ee1bb76e7391b93eb12"

user> (->> (slurp "./LICENSE.txt")
           (map int)
           s/sha-1
           (map (partial format "%02x"))
           (apply str))
"642a60a26df2120265996882e3d3ac5fa3691476"
```
