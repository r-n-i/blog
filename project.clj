(defproject blog "0.1.0-SNAPSHOT"
  :dependencies [[org.clojure/clojure "1.8.0"]
                 [org.clojure/clojurescript "1.9.229"]
                 [reagent "0.6.0"]
                 [re-frame "0.9.2"]
                 [day8.re-frame/http-fx "0.1.2"]
                 [cljs-ajax "0.5.9"]
                 [org.clojure/core.async "0.2.391"]
                 [re-com "2.0.0"]
                 [compojure "1.5.0"]
                 [yogthos/config "0.8"]
                 [ring/ring-json "0.4.0"]
                 [korma "0.4.0"]
                 [migratus "0.8.13"]
                 [mysql/mysql-connector-java "5.1.6"]
                 [buddy "1.3.0"]
                 [markdown-clj "0.9.99"]
                 [ring "1.4.0"]]

  :plugins [[lein-cljsbuild "1.1.4"]
            [migratus-lein "0.4.7"]]

  :min-lein-version "2.5.3"

  :source-paths ["src/clj"]

  :clean-targets ^{:protect false} ["resources/public/js/compiled" "target"]

  :figwheel {:css-dirs ["resources/public/css"]
             :ring-handler blog.handler/dev-handler}

  :profiles
  {:dev
   {:dependencies [[binaryage/devtools "0.8.2"]]

    :plugins      [[lein-figwheel "0.5.9"]]
    }}

  :cljsbuild
  {:builds
   [{:id           "dev"
     :source-paths ["src/cljs"]
     :figwheel     {:on-jsload "blog.core/mount-root"}
     :compiler     {:main                 blog.core
                    :output-to            "resources/public/js/compiled/app.js"
                    :output-dir           "resources/public/js/compiled/out"
                    :asset-path           "js/compiled/out"
                    :source-map-timestamp true
                    :preloads             [devtools.preload]
                    :external-config      {:devtools/config {:features-to-install :all}}
                    }}

    {:id           "min"
     :source-paths ["src/cljs"]
     :jar true
     :compiler     {:main            blog.core
                    :output-to       "resources/public/js/compiled/app.js"
                    :optimizations   :advanced
                    :closure-defines {goog.DEBUG false}
                    :pretty-print    false}}


    ]}

  :main blog.server

  :aot [blog.server]

  :uberjar-name "blog.jar"

  :prep-tasks [["cljsbuild" "once" "min"] "compile"]

  :migratus {:store :database
             :migration-dir "migrations"
             :db {:classname "com.mysql.jdbc.Driver"
                  :subprotocol "mysql"
                  :subname "//localhost/blog"
                  :user "root"
                  :password ""}}
  )
