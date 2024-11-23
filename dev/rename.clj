#!/usr/bin/env  bb

(require
  '[babashka.fs :as fs]
  '[clojure.string :as str]
  '[utils :refer [cli? base-path db]])

(defn -main [& args]
  (try (let [[_ n n'] (->> (str/join " " args)
                           (re-find #"(.*)\s+:\s+(.*)"))
             _ (assert (some? n))
             _ (assert (some? n'))
             [n n'] (map #(str (str/replace % " " "_") ".html") [n n'])
             path-str-bases [(str (when-not cli? "../") "post_drafts/")
                             (str (when-not cli? "../") "posts/")
                             (str base-path "drafts/")]
             old-path-str->new (->> path-str-bases
                                    (keep (fn [base-path-str]
                                            (when (fs/exists? (str base-path-str n))
                                              [(str base-path-str n) (str base-path-str n')])))
                                    (into {}))]
         (if (seq old-path-str->new)
           (do ; Move all files
               (doseq [[old-path-str new-path-str] old-path-str->new]
                 (fs/move old-path-str new-path-str))
               ; Update db
               (swap! db update :posts
                 (fn [m] (update-keys m #(if (= % n) n' %))))

               (println (str "Renamed " n " -> " n')))
           (println (str "ERROR: " n " doesn't exist"))))
       (catch java.lang.AssertionError _e
         (println (str "Usage: old name : new name")))
       (catch java.nio.file.FileAlreadyExistsException _e
         (println (str "ERROR: file already exists")))
       (catch Exception e
         (println "ERROR: " e))))

(when cli?
  (apply -main *command-line-args*))

(comment
  (-main "foo" ":" "foo bar")
  (-main "foo bar" ":" "foo")
  )
