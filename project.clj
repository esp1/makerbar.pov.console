(defproject makerbar.pov.console "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :profiles {:dev {:dependencies [[criterium "0.4.3"]]}}
  :dependencies [[org.clojure/clojure "1.6.0"]
                 [prismatic/hiphip "0.2.0"]
                 [quil "2.0.0-SNAPSHOT"]]
  :aot [makerbar.pov.console.ui]
  :main makerbar.pov.console.ui)
