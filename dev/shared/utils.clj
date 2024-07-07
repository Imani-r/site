(ns utils
  (:require
    [clojure.edn :as edn]
    [clojure.java.io :as io]
    [clojure.pprint :refer [pprint]]))

; ----------------------------------------------------------------------------------------------------------------------

(def cli?
  (some? (System/getProperty "babashka.file"))) ; not= *file* ever, as *file* is e.g. compile.clj

(def base-path
  (when cli? "dev/"))

; --- db.edn -----------------------------------------------------------------------------------------------------------

(def db
  (atom
    (with-open [r (java.io.PushbackReader. (io/reader (str base-path "db.edn")))]
      (edn/read r))))

; Keep dev/db.edn in sync with a watch on the atom
(add-watch db :sync
  (fn [_key _ref _old-state new-state]
    (with-open [w (io/writer (str base-path "db.edn"))]
      (pprint new-state w))))

(comment
  db
  (swap! db assoc :foo "bar")
  (swap! db dissoc :foo)
  )

; ----------------------------------------------------------------------------------------------------------------------
