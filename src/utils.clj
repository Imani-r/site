(ns utils
  (:require
    [clojure.string :as str]
    [hyperfiddle.rcf :refer [tests]])
  ; NOTE: 'import', not 'require', to pull in Java libraries.
  (:import
    [java.io StringReader StringWriter]
    [javax.xml.transform OutputKeys TransformerFactory]
    [javax.xml.transform.stream StreamResult StreamSource]))

; --- HTML -------------------------------------------------------------------------------------------------------------

(defn pphtml
  "That is, 'Pretty-print HTML'.

  With thanks to https://nakkaya.com/2010/03/27/pretty-printing-xml-with-clojure/."
  [html-str]
  (print
    (let [in          (StreamSource. (StringReader. html-str))
          writer      (StringWriter.)
          out         (StreamResult. writer)
          transformer (.newTransformer (TransformerFactory/newInstance))]
      (.setOutputProperty transformer OutputKeys/INDENT "yes")
      (.setOutputProperty transformer "{http://xml.apache.org/xslt}indent-amount" "2")
      (.setOutputProperty transformer OutputKeys/METHOD "xml")
      (.transform transformer in out)
      (-> out .getWriter .toString))))

; --- Strings ----------------------------------------------------------------------------------------------------------

(defn strip-leading-substring
  [s substring]
  (if (str/starts-with? s substring)
    (apply str (drop 1 (seq s)))
    s))

(tests
  (strip-leading-substring "" "a")  := ""
  (strip-leading-substring "a" "a") := ""
  (strip-leading-substring "b" "a") := "b"
  (strip-leading-substring "ab" "a") := "b"
  (strip-leading-substring "abc" "a") := "bc"
  )

(defn internal-href?
  [href]
  (let [subdomain-and-domain (re-find #"(?<=^https?://)[^/]+" href)]
    (or (nil? subdomain-and-domain)
        (str/includes? subdomain-and-domain "wildinginprogress.com"))))

(tests
  (internal-href? "http://wildinginprogress.com") := true
  (internal-href? "https://wildinginprogress.com") := true
  (internal-href? "https://www.wildinginprogress.com") := true
  (internal-href? "https://www.subdomain.wildinginprogress.com") := true
  (internal-href? "/some-local-link") := true
  (internal-href? "some-local-link") := true
  (internal-href? "https://public.tableau.com/...") := false
  )
