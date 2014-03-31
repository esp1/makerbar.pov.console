(defproject makerbar.pov.console "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :profiles {:dev {:dependencies [[criterium "0.4.3"]]}}
  :dependencies [[extrapixel/gifAnimation "d734273"]
                 [org.clojure/clojure "1.6.0"]
                 [org.processing/core "2.1.1"]
                 [org.processing/gstreamer-java "2.1.1"]
                 [org.processing/jna "2.1.1"]
                 [org.processing/video "2.1.1"]
                 [prismatic/hiphip "0.2.0"]]
  :aot [makerbar.pov.console.ui]
  :main makerbar.pov.console.ui)
