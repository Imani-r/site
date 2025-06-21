(ns site
  (:require
    [babashka.fs :as fs]
    [clojure.pprint :refer [pprint]]
    [clojure.string :as str]
    [clojure.walk :as walk]
    [hiccup2.core :as h]
    [hyperfiddle.rcf :refer [tests]]
    [markdown-to-hiccup.core :as m]
    [utils :as u]))

; --- useful, top-level data -------------------------------------------------------------------------------------------

(def pages
  "Ordered as they will be rendered by the `navbar` function, below."
  [:garden :portfolio :home :about :now])
(def pages-set (set pages))
(def p5-pages-set #{:home})

(def index-page-base-path "assets/html/")

(def page->href
  (-> {:about     "about.html"
       :garden    "garden.html"
       :home      "index.html"
       :now       "now.html"
       :portfolio "portfolio.html"}
      (update-vals #(str "/" ; <-- make all hrefs absolute
                         (if (= "index.html" %) %
                           (str index-page-base-path %))))))

(def page->title
  {:about     "About"
   :garden    "The Garden"
   :home      "Wilding in Progress"
   :now       "Now"
   :portfolio "Portfolio"})

(def base-posts-path "garden_posts/")
(def base-rendered-posts-path "assets/html/garden/")

; --- post file fns ----------------------------------------------------------------------------------------------------

(defn garden-post-filename?
  [s]
  (and (string? s)
       (boolean (re-find #"^\d+_\w+(?:\.(?:html|md))?$" s))))

(tests
  (garden-post-filename? "000_hello_world.html") := true
  (garden-post-filename? "000_hello_world.md") := true
  (garden-post-filename? "000_hello_world") := true
  (garden-post-filename? "_hello_world.html") := false
  (garden-post-filename? "000_hello_world.pdf") := false
  (garden-post-filename? "000.html") := false
  (garden-post-filename? "") := false
  (garden-post-filename? :hello-world) := false
  ; ...
  )

(defn calc-full-post-path
  [post]
  (cond
    (fs/exists? (str base-posts-path post)) (str base-posts-path post)
    (fs/exists? (str base-posts-path post ".md")) (str base-posts-path post ".md")
    (fs/exists? (str base-posts-path post ".html")) (str base-posts-path post ".html")))

(defn calc-post-name
  "That is, the filename excluding the file extension. E.g. \"garden_posts/foo.md\" --> \"foo\"."
  [full-post-path]
  (-> full-post-path
      (str/split #"/")
      (peek)
      (str/split #"\.")
      (first)))

(defn post-file-ext
  [full-post-path]
  (peek (str/split full-post-path #"\.")))

(defn read-post-metadata
  "For markdown files, tries to read the leading ```clojure <metadata map>``` block.

  For HTML files, tries to read the leading `<!-- key1=val1, key2=val2, ... -->` comment."
  [full-post-path]
  (cond-> (slurp full-post-path)
    (= "md" (post-file-ext full-post-path))
    (->> (re-find #"(?s)(?<=```clojure).*(?=```)"))

    (= "html" (post-file-ext full-post-path))
    (->> (re-find #"(?s)(?<=<!--).+(?=-->)"))

    :always
    (some-> (read-string))))

(defn read-post-title
  "First, attempts to read the post title from metadata. If not specified, will try to read it from the file's contents
  instead:

   - Markdown file --> the first `# header`
   - HTML file     --> the first <h1>"
  [full-post-path]
  (or (:title (read-post-metadata full-post-path))
      (cond-> (slurp full-post-path)
        (= "md" (post-file-ext full-post-path))
        (->> (re-find #"(?<=#\s+).+"))

        (= "html" (post-file-ext full-post-path))
        (->> (re-find #"(?s)(?<=<h1>).+(?=</h1>)"))

        :always
        (some-> (str/trim)))))

(comment
  (read-post-metadata "garden_posts/001_how_common_is_your_birthday_uk_edition.md")
  ; => {:some "metadata"}
  (read-post-metadata "garden_posts/005_rhodonea_curve_rose_1.html")
  ; => {:title "Sketch: Rhodonea Curve Rose 1", :p5? true}

  (read-post-title "garden_posts/001_how_common_is_your_birthday_uk_edition.md")
  ; => "How Common is Your Birthday? UK Edition"

  (read-post-title "garden_posts/005_rhodonea_curve_rose_1.html")
  ; => "Sketch: Rhodonea Curve Rose 1"
  )

; --- hiccup: index pages ----------------------------------------------------------------------------------------------

(defn about-page []
  [:div {:class "content-wrapper"}
   [:h1 "About"]
   [:div {:class "float-text-around-image"}
    [:div {:id "profile_photo", :class "img-container"}
     [:img {:src "/assets/img/profile_photo.jpeg"
            :alt "Profile Picture: Standing next to a colourful wall in sunny Lisbon"}]]

    [:p (str
          "When it comes to data visualisation, Faith believes that beauty is not a "
          "frill— it's what lures people in and invites them to dig deeper and "
          "engage with the underlying information. In her work, she strives to "
          "create data visualisations that are not only supported by rigorous "
          "analysis, but are also visually appealing. This combination is crucial "
          "because beauty without insight can be misleading, and insight without "
          "beauty is unremarkable.")]

    [:p (str "After graduating from Dartmouth College with an Economics degree, "
             "Faith began her career as a Product Lead at a global non-profit organization. "
             "There, her exposure to data analytics and UX/UI design sparked her interest in "
             "visual storytelling through data. She joined The Data School to further develop "
             "her expertise in a collaborative environment where she could both learn "
             "and share knowledge while delivering creative, analytical solutions for clients.")]]])

(defn garden-page []
  (let [n 5
        last-n-posts (->> (fs/list-dir base-posts-path)
                          (map fs/file-name)
                          (sort #(compare %2 %1))
                          (take 5 #_n)
                          (vec))
        last-n-titles (mapv #(read-post-title (str base-posts-path %)) last-n-posts)
        last-n-hrefs (mapv #(str "/" base-rendered-posts-path (calc-post-name %) ".html") last-n-posts)]

    [:div {:class "content-wrapper"}
     [:ul (for [i (range n)]
            [:li
             [:a {:href (get last-n-hrefs i)}
              (get last-n-titles i)]])]]))

(defn home-page []
  (list
    [:div {:class "sketch-wrapper"}
     [:article {:class "homingquotes"}
      [:p "\"Consecrate delight ✨ with your attention.\" - Visa"]]]
    [:script {:src "assets/js/08_flowers_sketch.js"}]))

(defn now-page []
  [:div {:class "content-wrapper"}
   [:p "Coming soon"]])

(defn portfolio-page []
  [:div {:class "content-wrapper"}
   [:a {:href "https://public.tableau.com/app/profile/faith5698/vizzeshttps://public.tableau.com/app/profile/faith5698/vizzes"
        :target "_blank"}
    "Currently on Tableau Public"]])

(def page->hiccup-fn
  {:garden    #'garden-page
   :portfolio #'portfolio-page
   :home      #'home-page
   :about     #'about-page
   :now       #'now-page})

; --- hiccup: general --------------------------------------------------------------------------------------------------

(defn navbar
  "Returns hiccup for a navbar, with the page title wrapped in an <h1>."
  [page]
  [:div.topnav
   (->> pages
        (map (fn [p]
               [:a {:href (page->href p)}
                (if (= p page)
                  [:h1 (page->title p)]
                  (page->title p))])))])


(defn head [title p5?]
  [:head
   [:meta {:charset "utf8"}]
   [:meta {:name "viewport", :content "width=device-width, initial-scale=1"}]
   [:title title]
   [:link {:rel "stylesheet", :href "/assets/css/reset.css"}]
   [:link {:rel "stylesheet", :href "/assets/css/style.css"}]
   (when p5? [:script {:src "https://cdnjs.cloudflare.com/ajax/libs/p5.js/1.9.0/p5.min.js"}])

   ; TODO all the other stuff
   ;<meta name="description" content="">
   ;<meta property="og:title" content="">
   ;<meta property="og:type" content="">
   ;<meta property="og:url" content="">
   ;<meta property="og:image" content="">
   ;<meta property="og:image:alt" content="">
   ;<link rel="icon" href="/favicon.ico" sizes="any">
   ;<link rel="icon" href="/icon.svg" type="image/svg+xml">
   ;<link rel="apple-touch-icon" href="icon.png">
   ;<link rel="manifest" href="site.webmanifest">
   ;<meta name="theme-color" content="#fafafa"> -->
   ])

(defn wrap-index-page-boilerplate
  [hiccup-body page]
  (let [title (page->title page)
        p5?   (contains? p5-pages-set page)]
    [:html {:lang "en"}
     (head title p5?)
     [:body hiccup-body]]))

(defn wrap-garden-post-boilerplate
  [{:keys [:hiccup-body :title :metadata]}]
  [:html {:lang "en"}
   (head title (:p5? metadata))
   [:body hiccup-body]])


; --- REPL fn helpers (not called directly) ----------------------------------------------------------------------------

(defn- parse-md->hiccup-body+title+metadata
  [markdown-str full-post-path]
  (let [[head & tail :as hiccup-body-els]
        (-> markdown-str
            (m/md->hiccup)
            ; ^Returns [:html {} [:head {}] [:body {} ...]], but we just want the elided bit. So:
            (get 3)
            (->> (drop 2)))

        metadata-code-block-present?
        (= :pre (first head))]

    {:hiccup-body (if metadata-code-block-present? tail hiccup-body-els)
     :metadata    (read-post-metadata full-post-path)
     :title       (read-post-title full-post-path)}))

(defn- parse-html->hiccup-body+title+metadata [html full-post-path]
  {:hiccup-body (h/raw html)
   :title       (read-post-title full-post-path)
   :metadata    (read-post-metadata full-post-path)})

(defn- expand-images
  [hiccup]
  (walk/postwalk
    (fn [form]
      (if (and (vector? form) (= :img (first form)))
        (let [m (-> (get-in form [1 :alt])
                    (str/split #"\s*,\s*")
                    (->> (mapv #(str/split % #"="))
                         (into {}))
                    (update-keys keyword))]
          [:div {:id (:id m), :class (str "img-container " (:class m))}
           (assoc-in form [1 :alt] (:alt m))])

        form))
    hiccup))

; --- REPL fns ---------------------------------------------------------------------------------------------------------

(defn render!
  "That is, 'render page or post'.

  To render a top-level index page, pass that page's keyword ID (e.g. `:home`). To render a post, pass the filename of
  its markdown file in `garden_posts/` (e.g. `\"001_how_common_is_your_birthday_uk_edition\", excluding the extension).

  The second argument, `opts`, is an optional map. Include `:safe? false` to (over)write the HTML output to a file
  (whose name is returned on success). By default, `:safe` is `true` and instead, the generated HTML is pretty-printed."
  [page-kw-or-post-filename
   & {:keys [safe? print-hiccup?]
      :or {safe? true}
      :as _opts}]

  ; Validate page-kw-or-post-filename before continuing
  {:pre [(or (contains? pages-set page-kw-or-post-filename)
             (garden-post-filename? page-kw-or-post-filename))]}

  (let [page (when (keyword? page-kw-or-post-filename) page-kw-or-post-filename)
        full-post-path (when (string? page-kw-or-post-filename) (calc-full-post-path page-kw-or-post-filename))
        ext (when full-post-path (re-find #"\.[a-zA-Z]+$" full-post-path))
        hiccup (cond
                 page
                 (-> ((page->hiccup-fn page))
                     (conj (navbar page))
                     (wrap-index-page-boilerplate page)
                     #_(expand-images))

                 full-post-path
                 (cond-> (slurp full-post-path)
                   (= ".md" ext)   (parse-md->hiccup-body+title+metadata full-post-path)
                   (= ".html" ext) (parse-html->hiccup-body+title+metadata full-post-path)
                   :always       (wrap-garden-post-boilerplate)
                   :always       (expand-images)))
        _ (when print-hiccup? (pprint hiccup))
        html (str (h/html (h/raw "<!DOCTYPE html>") hiccup))]

    (if safe?
      (u/pphtml html)

      (let [output-path (cond
                          page
                          (u/strip-leading-substring (page->href page) "/")

                          full-post-path
                          (str base-rendered-posts-path (calc-post-name full-post-path) ".html"))]
        (spit output-path html)

        ; Return output-path
        output-path))))

(comment
  (render! :about #_{:safe? false})
  (render! :garden #_{:safe? false})
  (render! :home #_{:safe? false})
  (render! :now #_{:safe? false})
  (render! :portfolio #_{:safe? false})

  (render! "001_how_common_is_your_birthday_uk_edition" #_{:safe? false})
  (render! "002_my_last_10_years_in_books" #_{:safe? false})
  (render! "003_are_you_drinking_a_safe_amount_of_caffeine_tea_edition" #_{:safe? false})
  (render! "004_designing_deadly_flowers_in_tableau" #_{:safe? false})
  (render! "005_rhodonea_curve_rose_1" #_{:safe? false})
  )
