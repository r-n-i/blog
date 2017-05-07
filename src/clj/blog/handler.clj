(ns blog.handler
  (:require [clojure.tools.logging :as log]
            [compojure.core :refer [GET POST DELETE defroutes]]
            [compojure.route :refer [resources]]
            [ring.util.response :refer [resource-response]]
            [ring.middleware.reload :refer [wrap-reload]]
            [ring.middleware.params :refer [wrap-params]]
            [ring.middleware.keyword-params :refer [wrap-keyword-params]]
            [ring.middleware.json :refer [wrap-json-params wrap-json-body wrap-json-response]]
            [clojure.data.json :as json]
            [buddy.core.nonce :as nonce]
            [buddy.core.codecs :as codecs]
            [buddy.auth :refer [authenticated? throw-unauthorized]]
            [buddy.auth.backends.token :refer [token-backend]]
            [buddy.auth.middleware :refer [wrap-authentication wrap-authorization]]
            [blog.core :as blog]))

(defn ok [d] {:status 200 :body d})
(defn bad-request [d] {:status 400 :body d})

(defn random-token
  []
  (let [randomdata (nonce/random-bytes 16)]
    (codecs/bytes->hex randomdata)))

(defn home
  [request]
  (log/info request)
  (if-not (authenticated? request)
    (throw-unauthorized)
    (ok {:status "Logged" :message (str "hello logged user"
                                        (:identity request))})))

(def authdata {:admin "secret"
               :test "secret"})

;; Global storage for generated tokens.
(def tokens (atom {}))

(defn login
  [request]
  (log/info "--- login ---")
  (log/info request)
  (let [username (get-in request [:params :username])
        password (get-in request [:params :password])
        valid? (some-> authdata
                       (get (keyword username))
                       (= password))]
    (if valid?
      (let [token (random-token)]
        (swap! tokens assoc (keyword token) (keyword username))
        (ok {:token token}))
      (bad-request {:message "wrong auth data"}))))

(defn my-authfn
  [req token]
  (get @tokens (keyword token)))

(def auth-backend
  (token-backend {:authfn my-authfn :token-name "Bearer"}))

(defroutes routes
  (GET "/" [] (resource-response "index.html" {:root "public"}))
  (GET "/auth" [] home)
  (POST "/login" [] login)
  (GET "/entries" [] (json/write-str (blog/select-entries)))
  (POST "/entries" req (json/write-str (blog/create-entry (:params req))))
  (DELETE "/entries" [id] (json/write-str (blog/delete-entry id)))
  (resources "/"))

(def dev-handler
  (->
    #'routes
    (wrap-authorization auth-backend)
    (wrap-authentication auth-backend)
    wrap-keyword-params
    wrap-json-params
    wrap-json-response
    wrap-reload))

(def handler routes)
