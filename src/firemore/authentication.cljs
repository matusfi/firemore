(ns firemore.authentication
  (:require
   [cljs.core.async :as async]
   [firemore.firebase :as firebase]
   [firemore.config :as config])
  (:require-macros
   [cljs.core.async.macros :refer [go-loop go]]))

(enable-console-print!)

(def FB firebase/FB)

(def signing-in? (atom false))

(def user-atom (atom nil))

(defn user-change-handler [js-user]
  (when js-user
    (reset!
     user-atom
     {:anonymous? (.-isAnonymous js-user)
      :uid        (.-uid js-user)})))

(-> FB firebase/auth (.onAuthStateChanged user-change-handler))

(defn login-anonymously!
  ([] (login-anonymously! FB))
  ([fb]
   (when-not @signing-in?
     (.signInAnonymously (firebase/auth fb))
     (reset! signing-in? true))))

(defn logout!
  ([] (logout! FB))
  ([fb] (.signOut (firebase/auth fb))))

(defn uid []
  (let [c (async/chan)]
    (go-loop []
      (if-let [uid (:uid @user-atom)]
        (async/put! c uid)
        (do
          (login-anonymously!)
          (async/<! (async/timeout 1000))
          (recur))))
    c))
