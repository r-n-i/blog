(ns blog.handler
  (:require [clojure.tools.logging :as log]
            [compojure.core :refer [GET POST DELETE PUT defroutes]]
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
            [buddy.hashers :refer [encrypt check]]
            [korma.db :refer [defdb mysql]]
            [korma.core :refer [defentity values where order insert select select* delete update set-fields]]
            [clj-time.core :refer [now]]
            [clj-time.coerce :refer [to-sql-time]]
            [blog.core :as blog]))

(defdb db (mysql {:db       "blog"
                  :user     "root"
                  :password ""
                  }))

(defentity entries)
(defentity users)

(defn ok [d] {:status 200 :body d})
(defn bad-request [d] {:status 400 :body d})

(defn random-token []
  (codecs/bytes->hex (nonce/random-bytes 16)))

(defn auth [request]
  (log/info request)
  (if-not (authenticated? request)
    (throw-unauthorized)
    (ok {:status "Logged"
         :message (str "hello logged user" (:identity request))})))

(def tokens (atom {}))

(defn password-matches?
    "Check to see if the password given matches the digest of the user's saved password"
    [email password]
    (some-> (select* users)
                       ; (fields :password_digest)
                        (where {:email email})
                        select
                        first
                        :encrypted_password
                        (->> (check password))))

(defn login [{{email :email password :password} :params}]
  (log/info "--- login ---")
  (log/info email)
  (log/info password)
    (if (password-matches? email password)
      (let [token (random-token)]
        (swap! tokens assoc (keyword token) {:email email :level :admin})
        (ok {:token token}))
      (bad-request {:message "wrong auth data"})))

(defn create-user [{{email :email password :password} :params :as request}]
  (log/info "--- create-user ---")
  (log/info request)
  (if-not (authenticated? request)
    (throw-unauthorized)
    (if-not (= :admin (:level (:identity request)))
      {:status 400 :body {:message "not admin"}}
      (do
        (insert users (values {:email email
                               :encrypted_password (encrypt password)
                               :created_at (to-sql-time (now))
                               :updated_at (to-sql-time (now))}))
        {:status 200 :body {:message "ok"}}))))

(defn reset-password [{{email :email} :identity {password :password} :params}]
  (update users (set-fields {:encrypted_password (encrypt password)}) (where {:email email})))

(def auth-backend
  (token-backend {:authfn (fn [req token] (get @tokens (keyword token)))
                  :token-name "Bearer"}))

(defroutes routes
  (GET "/" [] (resource-response "index.html" {:root "public"}))
  (GET "/auth" [] auth)
  (POST "/login" [] login)
  (PUT "/password" [] reset-password)
  (POST "/users" [] create-user)
  (GET "/entries" [] (json/write-str (select entries (order :id :desc))))
  (POST "/entries" req (json/write-str (insert entries (values (select-keys req [:title :body])))))
  (DELETE "/entries" [id] (json/write-str (delete entries (where {:id id}))))
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
