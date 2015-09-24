(defproject makerbar.pov.console "0.1.0-SNAPSHOT"
  :description "POV Console UI"
  :url "http://www.makerbar.com/"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :profiles {:dev {:dependencies [[criterium "0.4.3"]]}}
  :dependencies [[aleph "0.3.2"]
                 [byte-streams "0.1.10"]
                 [clj-serial "2.0.4-SNAPSHOT"]
                 [extrapixel/gifAnimation "d734273"]
                 [org.clojure/clojure "1.6.0"]
                 [org.clojure/core.async "0.1.278.0-76b25b-alpha"]
                 [org.clojure/math.numeric-tower "0.0.4"]
                 [org.clojure/tools.cli "0.3.1"]
                 [org.processing/core "2.1.1"]
                 [org.processing.video/gstreamer-java "2.1.1"]
                 [org.processing.video/jna "2.1.1"]
                 [org.processing.video/video "2.1.1"]
                 [prismatic/hiphip "0.2.0"]]
  :jvm-opts ["-Djava.library.path=lib/leapmotion-1.0.9.8391"
             "-Dgstreamer.library.path=lib/processing-2.1.1/video/macosx64"
             "-Dgstreamer.plugin.path=lib/processing-2.1.1/video/macosx64/plugins"]
  :aot [makerbar.pov.ui]
  :main makerbar.pov.ui)
