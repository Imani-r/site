#!/usr/bin/env  bb

(require
  '[babashka.fs :as fs]
  '[clojure.string :as str])

(if *command-line-args*
  (let [n (str/join "_" *command-line-args*)
        path-str (str "dev/drafts/" n ".html")]
    (if (fs/exists? path-str)
      (println (str "ERROR: " n ".html already taken"))
      (do (spit path-str (slurp "dev/post-template.html"))
          (println (str path-str " created")))))
  (println "Usage: dev/new.clj <posts file name>"))
