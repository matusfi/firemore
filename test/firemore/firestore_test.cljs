(ns firemore.firestore-test
  (:require
   [cljs.core.async :as async]
   [cljs.test :as t :include-macros true]
   [firemore.config :as config]
   [firemore.firestore :as sut]))

(t/deftest fundamentals-test
  (t/is (some? sut/OPTS))
  (t/is (some? sut/FB))
  (t/is (some? (sut/db sut/FB))))

(t/deftest keywordizing-test
  (t/are [k s] (= (sut/keywordize->str k) s)
    :a ":a"
    :a/b ":a/b")
  (t/are [s k] (= (sut/str->keywordize s) k)
    ":a" :a
    ":a/b" :a/b)
  (t/are [k] (-> k sut/keywordize->str sut/str->keywordize (= k))
    :a
    :a/b))

(t/deftest conversion-test
  (t/are [m] (= m (-> m sut/jsonify sut/clojurify))
    {}
    {:a "1" :b 2 :c 3.1}
    {:a.real.long.key/is-awesome "foo"}))

(t/deftest replace-timestamp-test
  (let [m {:a config/TIMESTAMP}]
    (t/is (not= config/TIMESTAMP
                (->  sut/replace-timestamp :a)))
    (t/is (some? (-> m sut/replace-timestamp :a)))))

(t/deftest get-and-set-test
  (let [reference ["test" "get-and-set-test"]
        m {:string "string-a"}]
    (t/async
     done
     (async/go
       (t/is (nil? (async/<! (sut/set-db! sut/FB reference m))))
       (t/is (= m (async/<! (sut/get-db sut/FB reference))))
       (done)))))

(t/deftest get-and-add-test
  (let [reference ["test"]
        m {:string "string-b"}]
    (t/async
     done
     (async/go
       (let [{:keys [id]} (async/<! (sut/add-db! sut/FB reference m))]
         (t/is (some? id))
         (t/is (= m (async/<! (sut/get-db sut/FB (conj reference id)))))
         (done))))))


