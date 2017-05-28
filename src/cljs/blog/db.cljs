(ns blog.db)

(def default-db
  {:name "re-frame"
   :entries []
   :focus nil
   :new-entry {:title nil :body nil}
   :mode :read
   :editor-mode :input
   :error nil
   :sign-error nil
   :user-form {:email nil :password nil}
   :show-menu false
   })
