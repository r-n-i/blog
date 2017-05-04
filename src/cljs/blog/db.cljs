(ns blog.db)

(def default-db
  {:name "re-frame"
   :entries []
   :focus nil
   :new-entry {:title nil :body nil}
   :mode :read
   :error nil
   })
