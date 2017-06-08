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

(extend-type java.sql.Timestamp
  json/JSONWriter
  (-write [date out]
    (json/-write (str date) out)))

(defdb db (mysql {:classname "com.mysql.cj.jdbc.Driver"
                  :db        "blog"
                  :user      "blog"
                  :password  ""
                  }))

(defentity entries)
(defentity users)

(defn ok [d] {:status 200 :body d})
(defn bad-request [d] {:status 400 :body d})

(defn random-token []
  (codecs/bytes->hex (nonce/random-bytes 16)))

(defn auth [request]
  (if-not (authenticated? request)
    (throw-unauthorized)
    (ok {:status "Logged"
         :message (str "hello logged user" (:identity request))})))

(def tokens (atom {}))

(defn matched-user [email password]
  (when-let [user (-> (select* users)
                      (where {:email email})
                      select
                      first)]
    (if (check password (:encrypted_password user))
      user
      nil)))

(defn password-matches?
  [email password]
  (some-> (select* users)
          (where {:email email})
          select
          first
          :encrypted_password
          (->> (check password))))

(defn login [{{email :email password :password} :params}]
  (if-let [user (matched-user email password)]
    (let [token (random-token)]
      (swap! tokens assoc (keyword token) (select-keys user [:email :id :admin]))
      (ok {:token token}))
    (bad-request {:message "wrong auth data"})))

(defn encrypted-params [{password :password :as params}]
  (if password
    (-> params (dissoc :password) (assoc :encrypted_password (encrypt password)))
    params))

(defn update-user [{{id :id} :identity params :params :as request}]
  (if-not (authenticated? request)
    (throw-unauthorized)
    (do
      (update
        users
        (set-fields (-> params
                        (select-keys [:email :password])
                        encrypted-params
                        (assoc :updated_at (to-sql-time (now)))))
        (where {:id id}))
      {:status 200 :body {:message "ok"}})))

(defn force-create-user [email password admin]
  (insert users (values {:email email
                         :encrypted_password (encrypt password)
                         :admin admin
                         :created_at (to-sql-time (now))
                         :updated_at (to-sql-time (now))})))

(defn create-user [{{email :email password :password} :params :as request}]
  (log/info "===========")
  (log/info request)
  (if-not (authenticated? request)
    (throw-unauthorized)
    (if-not (:admin (:identity request))
      {:status 400 :body {:message "not admin"}}
      (do
        (force-create-user email password false)
        {:status 200 :body {:message "ok"}}))))

(defn create-entry
  [{{:keys [title body]} :params :as req}]
  (if-not (authenticated? req)
    (throw-unauthorized)
    (do
      (insert entries (values {:user_id (:id (:identity req))
                               :title title
                               :body body
                               :created_at (to-sql-time (now))
                               :updated_at (to-sql-time (now))}))
      {:status 200 :body {:message "ok"}})))

(defn delete-entry
  [{{:keys [id]} :params :as req}]
  (if-not (authenticated? req)
    (throw-unauthorized)
    (do
      (let [entry (first (select entries (where {:id id})))]
        (if (or (nil? entry) (not= (:id entry) id))
          {:status 404 :body {:message "not found"}}
          (do
            (delete entries (where {:id id}))
            {:status 200 :body {:message "ok"}}))))))

(defn get-entries
  [{{user_id :id} :identity :as req}]
  (->> (select entries (order :id :desc))
       (map #(assoc % :mine (= user_id (:user_id %))))
       (json/write-str)))

(def auth-backend
  (token-backend {:authfn (fn [req token] (get @tokens (keyword token)))
                  :token-name "Bearer"}))

(defroutes routes
  (GET "/" [] (resource-response "index.html" {:root "public"}))
  (GET "/auth" [] auth)
  (POST "/login" [] login)
  (POST "/users" [] create-user)
  (PUT "/user" [] update-user)
  (GET "/entries" [] get-entries)
  (POST "/entries" [] create-entry)
  (DELETE "/entries" [] delete-entry)
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
