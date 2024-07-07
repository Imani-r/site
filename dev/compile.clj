#!/usr/bin/env bb

(require
  '[babashka.fs :as fs]
  '[clojure.string :as str]
  '[utils :refer [cli? base-path db]])

; ----------------------------------------------------------------------------------------------------------------------

(def post-paths (fs/list-dir (str base-path "posts_content")))

(defn- modified?
  [path]
  (> (fs/file-time->millis (fs/last-modified-time path))
     (or (get-in @db [:posts (fs/file-name path) :file/last-modified])
         ##-Inf)))

(defn- content-changed?
  [path]
  (not= (hash (slurp (str path)))
        (get-in @db [:posts (fs/file-name path) :file/hash])))

(def template-re #"\{\{.*\}\}")

(defn wrap-boilerplate
  [raw-body title]
  (-> (slurp (str base-path "boilerplate.html"))
      (str/replace template-re {"{{title}}" title, "{{body}}" raw-body})))

(defn today []
  (.format (java.time.LocalDate/now)
           (java.time.format.DateTimeFormatter/ofLocalizedDate
             java.time.format.FormatStyle/LONG)))

(defn compile []
  (let [modified-files-paths (filter modified? post-paths)
        ; save new :last-modified times
        _ (swap! db update :posts
            #(merge-with merge %
               (reduce (fn [m file-path]
                         (assoc m
                           (fs/file-name file-path)
                           {:file/last-modified (fs/file-time->millis (fs/last-modified-time file-path))}))
                       {} modified-files-paths)))

        changed-files-paths (filter content-changed? modified-files-paths)
        ; save new `:hash`es
        _ (swap! db update :posts
            #(merge-with merge %
               (reduce (fn [m file-path]
                         (assoc m
                           (fs/file-name file-path)
                           {:file/hash (hash (slurp (str file-path)))}))
                       {} changed-files-paths)))

        retry-files-paths (filterv #(get-in @db [:posts (fs/file-name %) :retry?]) post-paths)
        ; 'consume' all :retry? flags
        _ (swap! db update :posts
            #(merge %
               (reduce (fn [m [file-name post-m]]
                         (assoc m file-name (dissoc post-m :retry?)))
                       {} %)))]

    ; recompile changed posts
    (doseq [file-path (concat changed-files-paths retry-files-paths)]
      (try (let [raw-body (slurp (str file-path))
                 draft? (boolean (re-find #"^\s*\<\!\-\- DRAFT" raw-body))
                 file-name (fs/file-name file-path)
                 ; NOTE: title extraction depends on the title element having id="title"
                 title (re-find #"(?<=id=\"title\">).*(?=<)" raw-body)

                 today (today)
                 published (if draft? "DRAFT" (get-in @db [:posts file-name :published] today))
                 last-updated (if (or (= published today) draft?) "" today)]

             (spit (str (when-not cli? "../")
                        (if draft? "drafts/" "posts/")
                        file-name)
                   (-> raw-body
                       (str/replace template-re {"{{published}}" published, "{{last-updated}}" last-updated})
                       (wrap-boilerplate title)))

             (println (str (if draft? "drafts/" "posts/") file-name " compiled"))

             ; record times
             (swap! db assoc-in [:posts file-name :last-compiled] (System/currentTimeMillis))
             (swap! db assoc-in [:posts file-name :post/published] published)
             (when (seq last-updated)
               (swap! db assoc-in [:posts file-name :post/last-updated] last-updated)))

           (catch Exception e
             ; make note that retry is needed
             (swap! db assoc-in [:posts (fs/file-name file-path) :retry?] true)
             (println (str (fs/file-name file-path) " failed to compile. Problem: " e)))))))


; ----------------------------------------------------------------------------------------------------------------------

(defn -main [& _args]
  (compile))

(when cli?
  (apply -main *command-line-args*))

; ----------------------------------------------------------------------------------------------------------------------

; TODO: collect post tags; generate tag index page

; TODO 'edit' script to edit filename
; TODO 'delete' script to delete post draft (and clean up the db)

(comment
  (compile)
  )
