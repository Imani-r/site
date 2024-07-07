#!/usr/bin/env  bb

(require
  '[babashka.fs :as fs]
  '[clojure.string :as str]
  '[utils :refer [cli? base-path db]])

(defn -main [& args]
  (let [n (-> (str/join "_" args) (str ".html"))
        path-strs (->> [(str (when-not cli? "../") "drafts/" n)
                        (str (when-not cli? "../") "posts/" n)
                        (str base-path "posts_content/" n)]
                       (filter fs/exists?))]
    (if (seq path-strs)
      (do ; Delete all files
          (doseq [f path-strs] (fs/delete f))
          ; Remove from db
          (swap! db update :posts #(dissoc % n))

          (println (str "Deleted: " (str/join ", " path-strs))))
      (println (str "ERROR: " n " doesn't exist")))))

(when cli?
  (apply -main *command-line-args*))

(comment
  (-main "foo")
  )
