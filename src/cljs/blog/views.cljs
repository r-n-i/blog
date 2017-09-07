(ns blog.views
  (:require [re-frame.core :as re-frame]
            [reagent.core :as reagent]
            [markdown.core :as markdown]))

(defn react-raw [raw-html-str]
  "A React component that renders raw html."
  (.div (.-DOM js/React)
        (clj->js {:key (str (hash raw-html-str))
                  :dangerouslySetInnerHTML {:__html raw-html-str}})))

(defn highlight-code [html-node]
    (let [nodes (.querySelectorAll html-node "pre code")]
          (loop [i (.-length nodes)]
                  (when-not (neg? i)
                            (when-let [item (.item nodes i)]
                                        (.highlightBlock js/hljs item))
                            (recur (dec i))))))

(defn highlight [this]
    (let [node (reagent/dom-node this)]
          (highlight-code node)))

(defn modal []
  (fn []
    (let [user-form (re-frame/subscribe [:user-form])
          auth       @(re-frame/subscribe [:auth])
          sign-error (re-frame/subscribe [:sign-error])]
    [:div.modal.is-active
     [:div.modal-background]
     [:div.modal-content
      [:section.section
          [:h2.title {:style {:font-weight :bold}} (if auth "Setting" "Login")]
          [:p.subtitle {:style {:color :red}} @sign-error]
          [:p.subtitle "Email"]
          [:div.field
           [:p.control
            [:input.input {:value (:email @user-form)
                           :on-change #(re-frame/dispatch [:on-change-email (-> % .-target .-value)])}]]]
          [:p.subtitle "Password"]
          [:div.field
           [:p.control
            [:input.input {:type :password
                           :value (:password @user-form)
                           :on-change #(re-frame/dispatch [:on-change-password (-> % .-target .-value)])}]]]
         [:span
          [:a.button.is-primary.is-outlined
          {:on-click #(re-frame/dispatch [(if auth :create-user :login)])}
          (if auth "create user" "login")]
         (when auth [:a.button.is-primary.is-outlined
          {:on-click #(re-frame/dispatch [:update-user])
           :style {:margin-left "10px"}}
          "update"])]
         ]]

     [:button.modal-close {:on-click #(re-frame/dispatch [:hide-login-modal])}]]))
  )

(defn inputs []
  (fn []
    (let [new-entry @(re-frame/subscribe [:new-entry])]
      [:div.container
       [:div.field
        [:p.control
         [:input.input {:value (:title new-entry)
                        :placeholder "Title"
                        :on-change #()
                        :on-input #(re-frame/dispatch-sync [:on-change-title (-> % .-target .-value)])}]]]
       [:div.field
        [:p.control
         [:textarea.textarea {:value (:body new-entry)
                              :placeholder "Body"
                              :on-change #()
                              :on-input #(re-frame/dispatch-sync [:on-change-body (-> % .-target .-value)])}]]]])))

(defn preview []
  (fn []
    (reagent/create-class
      {:component-did-update highlight
       :reagent-render
       (fn []
         (let [new-entry @(re-frame/subscribe [:new-entry])]
           [:div.container
            [:p.title (:title new-entry)]
            [:div.content
             (react-raw (markdown/md->html (:body new-entry)))
             ]
            ]))})))

(defn input-preview []
  (fn []
    [:div.tile
     [:div.tile.is-5
      [inputs]]
     [:div.tile.is-1]
     [:div.tile.is-5
      [preview]]]))

(defn editor []
  (fn []
    (let [new-entry   @(re-frame/subscribe [:new-entry])
          editor-mode (re-frame/subscribe [:editor-mode])]
      [:section.section
       [:div.container
        [:div.column.is-8
         [:h2.title {:style {:font-weight :bold}} "New Entry"]
         ]
        [:div.column.is-12
         [:div.tabs
          [:ul
           [(if (= @editor-mode :input) :li.is-active :li)
            [:a {:on-click #(re-frame/dispatch [:switch-editor-mode :input])} "input"]]
           [(if (= @editor-mode :preview) :li.is-active :li)
            [:a {:on-click #(re-frame/dispatch [:switch-editor-mode :preview])} "preview"]]
           [(if (= @editor-mode :both) :li.is-active :li)
            [:a {:on-click #(re-frame/dispatch [:switch-editor-mode :both])} "both"]]
           ]]
         ]
        [:div.column.is-12
         (case @editor-mode
           :input   [inputs]
           :preview [preview]
           :both    [input-preview])
         ]
        [:div.column.is-2
         [:a.button.is-primary.is-outlined
          {:on-click #(re-frame/dispatch [:post])}
          "save"]
         ]
        ]])))

(defn entry [{:keys [title body updated_at id mine]}]
  (fn []
    (reagent/create-class
      {:component-did-mount highlight
       :reagent-render
       (fn []
         (let [auth @(re-frame/subscribe [:auth])]
           [:section.section
            [:div.container
             [:div.columns
              [:div.column.is-4
               [:p.subtitle (str updated_at "  ")
                (when mine [:a {:on-click #(re-frame/dispatch [:delete id])} "delete"])]
               [:h2.title {:style {:font-weight :bold}} title]]
              [:div.column.is-8
               [:div.content (react-raw (markdown/md->html body))]
               ]]]]))})))

(defn entries []
  (fn []
    (let [entries @(re-frame/subscribe [:entries])]
      [:div
       (for [entry- entries] ^{:key entry-}
         [entry entry-])])))

(defn nav []
  (fn []
    (let [show-menu @(re-frame/subscribe [:show-menu])
          auth @(re-frame/subscribe [:auth])]
      [:div.nav
       [:div.nav-left
        [:a.nav-item.is-brand "RNI Developer's Blog"]]
       [:div.nav-center]
       [:span.nav-toggle
        {:on-click #(re-frame/dispatch [:toggle-menu])
         :class (if show-menu :is-active "")}
        [:span]
        [:span]
        [:span]]
       [:div.nav-right.nav-menu
        {:class (if show-menu :is-active "")}
        [:a.nav-item {:on-click #(re-frame/dispatch [:show-login-modal])} (if auth "setting" "login")]
        (when auth [:a.nav-item {:on-click #(re-frame/dispatch [:new-entry])} "new entry"])
        ]])))

(defn header []
  (fn []
    [:section.hero.is-primary {:style {:color :pink}}
     [:div.hero-body
      [:div.container
       [:div.columns.is-vcentered
        [:div.column
         [:h1.title "RNI Developer's Blog"]
         [:p.subtitle "株式会社リサーチ・アンド・イノベーションの開発者ブログです"]]]]]]))

(defn main-panel []
  (fn []
    (let [error            @(re-frame/subscribe [:error])
          mode             @(re-frame/subscribe [:mode])
          show-login-modal @(re-frame/subscribe [:show-login-modal])]
      [:div
       [nav]
       [header]
       (when error [:div.notification.is-warning error])
       (when (= mode :edit) [editor])
       [entries]
       (when show-login-modal [modal])
       ]
      )))
