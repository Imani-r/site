(ns site
  (:require
    [clojure.pprint :refer [pprint]]
    [clojure.string :as str]
    [clojure.walk :as walk]
    [hiccup2.core :as h]
    [hyperfiddle.rcf :refer [tests]]
    [markdown-to-hiccup.core :as m]))

(def pages [:garden :portfolio :home :about :now])
(def pages-set (set pages))
(def p5-pages-set #{:home})

(def index-page-base-path "assets/html/")

(def page->href
  (-> {:garden    "the-garden.html"
       :portfolio "portfolio.html"
       :home      "index.html"
       :about     "about.html"
       :now       "now.html"}
      (update-vals #(if (= "index.html" %) %
                      (str index-page-base-path %)))))

(def page->title
  {:garden    "The Garden"
   :portfolio "Portfolio"
   :home      "Wilding in Progress"
   :about     "About"
   :now       "Now"})

(defn garden-post-filename?
  [s]
  (and (string? s)
       (boolean (re-find #"^\d+_\w+\.html" s))))

(tests
  (garden-post-filename? "000_hello_world.html") := true
  (garden-post-filename? "_hello_world.html") := false
  (garden-post-filename? "000_hello_world.pdf") := false
  (garden-post-filename? "000.html") := false
  (garden-post-filename? "") := false
  (garden-post-filename? :hello-world) := false
  ; ...
  )

(defn expand-images
  [hiccup]
  (walk/postwalk
    (fn [form]
      (if (and (vector? form) (= :img (first form)))
        (let [m (-> (get-in form [1 :alt])
                    (str/split #",")
                    (->> (mapv #(str/split % #"="))
                         (into {}))
                    (update-keys keyword))]
          [:div {:class (:class m)}
           [:div {:id "profile_photo", :class "img-container"}
            (assoc-in form [1 :alt] (:alt m))]])

        form))
    hiccup))


; --- hiccup: index pages ---

(defn home-page []
  (list
    [:div {:class "sketch-wrapper"}
     [:article {:class "homingquotes"}
      [:p "\"Consecrate delight ✨ with your attention.\" - Visa"]]]
    [:script {:src "assets/js/08_flowers_sketch.js"}]))

(defn about-page []
  [:div {:class "content-wrapper"}
   ;[:h1 "About"]

   (m/component (m/md->hiccup
                  (slurp "some-post.md")))])

(def page->hiccup-fn
  {;:garden    "the-garden.html"
   ;:portfolio "portfolio.html"
   :home      #'home-page
   :about     #'about-page
   ;:now       "now.html"
   })

; --- hiccup: general ---

(defn navbar
  "Returns hiccup for a navbar, with `page` wrapped in an `h1`."
  [page]
  [:div.topnav
   (->> pages
        (map (fn [p]
               [:a {:href (page->href p)}
                (if (= p page)
                  [:h1 (page->title p)]
                  (page->title p))])))])

(defn wrap-boilerplate
  [hiccup-body
   & {:keys [title p5?]
      :or {title (page->title :home), p5? false}
      :as opts}]
  [:html {:lang "en"}
   [:head
    [:meta {:charset "utf8"}]
    [:meta {:name "viewport", :content "width=device-width, initial-scale=1"}]
    [:title title]
    [:link {:rel "stylesheet", :href "assets/css/reset.css"}]
    [:link {:rel "stylesheet", :href "assets/css/style.css"}]
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
    ]
   [:body hiccup-body]])

; --- REPL fns ---

(defn render!
  "That is, 'render page or post'.

  To render a top-level index page, pass that page's keyword ID (e.g. `:home`). To render a post, pass the filename of
  its markdown file in `garden_posts/` (e.g. `\"001_how_common_is_your_birthday_uk_edition.html\"`).

  The second argument, `opts`, is an optional map. Include `:safe? true` to directly return the rendered HTML, instead
  of write it to the corresponding output file.

  NOTE: The default `:safe? false` behaviour is why the function ends, by convention, in a `!` — this indicates that it
  is side-effectful; it does something 'off to the side' (write to a file) before returning.

  By default, returns the full path of the output file on success."
  [page-kw-or-post-filename & {:keys [safe?] :as _opts, :or {safe? false}}]

  ; Validate page-kw-or-post-filename before continuing
  {:pre [(or (contains? pages-set page-kw-or-post-filename)
             (garden-post-filename? page-kw-or-post-filename))]}

  (let [page   (when (keyword? page-kw-or-post-filename) page-kw-or-post-filename)
        post   (when (string? page-kw-or-post-filename) page-kw-or-post-filename)
        hiccup (cond
                 page
                 (-> ((page->hiccup-fn page))
                     (conj (navbar page))
                     (wrap-boilerplate {:title (page->title page) :p5? (contains? p5-pages-set page)})
                     (expand-images))

                 post
                 :TODO
                 )
        html   (str (h/html (h/raw "<!DOCTYPE html>") hiccup))]

    (if safe?
      html

      (let [output-path (cond
                          page
                          (page->href :home #_page)

                          post
                          "foo")]
        (spit output-path html)

        ; Return output-path
        output-path))))

(comment
  (render! :home {:safe? true})
  (render! :home)

  (render! "000_hello.html" {:safe? true})
  (render! "000_hello.html")
  )

; Scratch code
(comment
  (str (h/html [:div#foo [:strong "foo"]]))

  (-> (m/md->hiccup "| foo | bar |\r| --- | --- |\r| yo | hey |")
      (h/html)
      (str))

  [:div.foo {:class "foo bar baz"}]

  )
