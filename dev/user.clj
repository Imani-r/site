(ns user
  (:require
    [clojure.repl :refer :all]
    [hyperfiddle.rcf]
    [site :refer :all]))

; Enable the 'tests' forms in src/site.clj, by default
(hyperfiddle.rcf/enable!)
