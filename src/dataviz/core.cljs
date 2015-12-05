(ns dataviz.core
  (:require
    [clojure.string :as str]
    [cemerick.url :as u]
    [cljs.reader :refer [read-string]]
    [cljs-http.client :as http]
    [cljs.core.async :refer [<!]]
    [dataviz.ui :as ui]
    [dataviz.connector :as c]
    [cljs.core.async :as async]
    [datascript.core :as data]
    [datascript.transit :as dt]
    [quiescent.core :as q :include-macros true]
    [quiescent.dom :as dom])
  (:require-macros 
    [cljs.core.async.macros :refer [go]])
  )

(enable-console-print!)

; Many thanks to http://leonid.shevtsov.me/en/oauth2-is-easy
 (def oauth2-params
   {:client-id "05ed84dcbf548234924a"
    :client-secret "a4356b5a8c532a6f52edf6df9f191b1ddea7cb42"
    :authorize-uri  "https://github.com/login/oauth/authorize"
    :redirect-uri "http://localhost"
    :access-token-uri "https://github.com/login/oauth/access_token"
    :scope "public_repo"}) 

(defn authorize-uri [client-params csrf-token]
   (str
     (:authorize-uri client-params)
     "?response_type=code"
     "&client_id="
     (u/url-encode (:client-id client-params))
     "&redirect_uri="
     (u/url-encode (:redirect-uri client-params))
     "&scope="
     (u/url-encode (:scope client-params))))

; (defn get-authentication-response [client-params code]
;   (go
;     (read-string
;       (:body (<! (http/post (:access-token-uri client-params)
;                             {:query-params {:code          code
;                                      :grant_type    "authorization_code"
;                                      :client_secret (:client-secret client-params)
;                                      :client_id     (:client-id client-params)
;                                      :redirect_uri  (:redirect-uri client-params)}
;                        :as          :json
;                        }))))))

(defn get-authentication-response [client-params code]
  (go
    (let [response (<! (http/post "https://github.com/login/oauth/access_token" 
                            {:form-params {
                                     :code          code
                                     :client_secret (:client-secret client-params)
                                     :client_id     (:client-id client-params)
                                     :redirect_uri  (:redirect-uri client-params)}}))
      ]
      (prn response)))
  )

    ; (go (let [response (<! (http/post (:access-token-uri client-params)
    ;                   {:json-params {:code          code
    ;                                  :grant_type    "authorization_code"
    ;                                  :client_secret (:client-secret client-params)
    ;                                  :client_id     (:client-id client-params)
    ;                                  :redirect_uri  (:redirect-uri client-params)}
    ;                    :as          :json
    ;                    }))]
    ;   (prn response)
    ;   (prn (:status response))
    ;   (prn (:body response)))))


(q/defcomponent SignIn 
  []
  (dom/div {}
    (dom/button {:onClick (fn [_]
                          (.replace js/location (authorize-uri oauth2-params)))}
      "Sign in with GitHub")))

(defn open-signin []
    (q/render (SignIn) js/document.body)
  )

(defn signin [auth-code]
    (println (get-authentication-response oauth2-params auth-code))
  )


(def user-schema {:user/access    {}
  })

(defonce user-conn (data/create-conn user-schema))

(defn db->string [db]
  (dt/write-transit-str db))

(defn string->db [s]
  (dt/read-transit-str s))

(defn persist [db]
  (js/localStorage.setItem "dataviz/userDB" (db->string db)))

(defn ^:export start 
  []
    ; (def auth-code (get 
    ;     (:query (u/url (-> js/window .-location .-href)))
    ;     "code"))
    ; (if (nil? auth-code) (open-signin) (signin auth-code))
    ; (when-let [stored (js/localStorage.getItem "dataviz/userDB")]
    ;   (let [stored-db (string->db stored)]
    ;     (reset! user-conn stored-db)))

    ; (data/transact! user-conn [{:db/id -1
    ;     :user/access "b77bc2a1280d59b6c26fee0122c46e7892d86f02"}])
    ; (persist @user-conn)

    

    (c/import)
    (ui/render)
  )
