#!/usr/bin/env  bb

(require
  '[babashka.fs :as fs]
  '[clojure.string :as str]
  '[clojure.edn :as edn]
  '[clojure.java.io :as io]
  '[clojure.pprint :refer [pprint]]
  '[hiccup2.core :as h])

; --- db.edn -----------------------------------------------------------------------------------------------------------

(def ^:dynamic db
  (with-open [r (java.io.PushbackReader. (io/reader "dev/db.edn"))]
    (edn/read r)))

(defn db-swap!
  [f & args]
  (let [db' (apply f db args)]
    ; save to file
    (with-open [w (io/writer "dev/db.edn")]
      (pprint db' w))
    ; save locally
    (alter-var-root #'db (constantly db'))
    ; return
    db'))

(comment
  db
  (db-swap! assoc :foo "bar")
  (db-swap! dissoc :foo)
  )

; ----------------------------------------------------------------------------------------------------------------------

(def post-paths (fs/list-dir "dev/posts_content"))

(defn- modified?
  [path]
  (> (fs/file-time->millis (fs/last-modified-time path))
     (or (get-in db [:posts (fs/file-name path) :file/last-modified])
         ##-Inf)))

(defn- content-changed?
  [path]
  (not= (hash (slurp (str path)))
        (get-in db [:posts (fs/file-name path) :file/hash])))

(def template-re #"\{\{.*\}\}")

(defn wrap-boilerplate
  [raw-body title]
  (-> (slurp "dev/boilerplate.html")
      (str/replace template-re {"{{title}}" title, "{{body}}" raw-body})))

(defn today []
  (.format (java.time.LocalDate/now)
           (java.time.format.DateTimeFormatter/ofLocalizedDate
             java.time.format.FormatStyle/LONG)))

(defn compile []
  (let [modified-files-paths (filter modified? post-paths)
        ; save new :last-modified times
        _ (db-swap! update :posts
            #(merge-with merge %
               (reduce (fn [m file-path]
                         (assoc m
                           (fs/file-name file-path)
                           {:file/last-modified (fs/file-time->millis (fs/last-modified-time file-path))}))
                       {} modified-files-paths)))

        changed-files-paths (filter content-changed? modified-files-paths)
        ; save new `:hash`es
        _ (db-swap! update :posts
            #(merge-with merge %
               (reduce (fn [m file-path]
                         (assoc m
                           (fs/file-name file-path)
                           {:file/hash (hash (slurp (str file-path)))}))
                       {} changed-files-paths)))

        retry-files-paths (filterv #(get-in db [:posts (fs/file-name %) :retry?]) post-paths)
        ; 'consume' all :retry? flags
        _ (db-swap! update :posts
            #(merge %
               (reduce (fn [m [file-name post-m]]
                         (assoc m file-name (dissoc post-m :retry?)))
                       {} %)))]

    ; recompile changed posts
    (doseq [file-path (concat changed-files-paths retry-files-paths)]
      (try (let [raw-body (slurp (str file-path))
                 file-name (fs/file-name file-path)
                 ; NOTE: title extraction depends on the title element having id="title"
                 title (re-find #"(?<=id=\"title\">).*(?=<)" raw-body)

                 today (today)
                 published (get-in db [:posts file-name :published] today)
                 last-updated (if-not (= published today) today "")]

             (spit (str "posts/" file-name)
                   (-> raw-body
                       (str/replace template-re {"{{published}}" published, "{{last-updated}}" last-updated})
                       (wrap-boilerplate title)))

             ; record times
             (db-swap! assoc-in [:posts file-name :last-compiled] (System/currentTimeMillis))
             (db-swap! assoc-in [:posts file-name :post/published] published)
             (when (seq last-updated)
               (db-swap! assoc-in [:posts file-name :post/last-updated] last-updated)))

           (catch Exception e
             ; make note that retry is needed
             (db-swap! assoc-in [:posts (fs/file-name file-path) :retry?] true)
             (println (str (fs/file-name file-path) " failed to compile. Problem: " e)))))))


; ----------------------------------------------------------------------------------------------------------------------

; TODO: 'new post' script, that templates things
; TODO: posts without some kind of 'DRAFT' header are automatically published with today's date if not yet published
; TODO: collect post tags; generate tag index page

(comment
  (compile)
  )
