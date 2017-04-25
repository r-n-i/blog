(ns blog.db)

(def default-db
  {:name "re-frame"
   :entries [{:id 0
              :title "title a"
              :body "sssss\nsssss"}
             {:id 1
              :title "title b"
              :body "oooo\nooooo"}
             ]
   :focus nil
   :new-entry {:title nil :body nil}
   :mode :read
   })
