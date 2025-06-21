(ns utils
  (:require
    [clojure.string :as str]
    [hyperfiddle.rcf :refer [tests]])
  ; NOTE: 'import', not 'require', to pull in Java libraries.
  (:import
    [java.io StringReader StringWriter]
    [javax.xml.transform OutputKeys TransformerFactory]
    [javax.xml.transform.stream StreamResult StreamSource]))

; ----------------------------------------------------------------------------------------------------------------------

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

; ----------------------------------------------------------------------------------------------------------------------

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
