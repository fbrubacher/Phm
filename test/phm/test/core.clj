(ns phm.test.core
  (use [lazytest.describe :only (describe do-it)]
       [lazytest.expect :only (expect)]
       [phm.connection]
       [phm.phm]))

(describe "when defining a new model"
          (do-it "should create a new atom with value 0 in its meta value"
                 (defredismodel "user" {:attributes ["name"] :collections ["friends"]})
                 (expect (= 0 (deref (:item_nbr (meta user)))))))

(describe "when defining a new member of that model"
          (do-it "should have an atom that the key has info about the occurrence"
                 (defredismodel "user" {:attributes ["name"] :collections ["friends"]})
                 (create-model-atom "user" "federico")
                 (expect (= "user:federico:" (:redis-key @user-federico)))))

(describe "when creating  a new member of that model"
          (do-it "should store to redis the attributes of that occurrence"
                 (defredismodel "user" {:attributes ["name"] :collections ["friends"]})
                 (create-model-atom "user" "federico")
                 (def pool1 (configure-connection {}))
                 (def connection1 (.getResource pool1))
                 (-> connection1 (.flushAll))
                 (create-redis-key user-federico {:name "john"})
                 (expect (= "john" (-> connection1 (.get "user:federico:name"))))))

(describe "when creating  a new member of that model"
          (do-it "should store to redis the attributes of that occurrence"
                 (defredismodel "user" {:attributes ["name"] :collections ["friends"]})
                 (def pool1 (configure-connection {}))
                 (def connection1 (.getResource pool1))
                 (-> connection1 (.flushAll))
                 (create-model-atom "user" "federico")
                 (generate-helper-funcs "user" "federico" user-federico)
                 (user-federico-add "friends" "jacinto")
                 (expect (= "jacinto" (-> connection1 (.spop "user:federico:friends"))))))

(describe "when  adding a friend to a member of that model"
          (do-it "should store the object in memory and in redis"
                 (defredismodel "user" {:attributes ["name"] :collections ["friends"]})
                 (def pool1 (configure-connection {}))
                 (def connection1 (.getResource pool1))
                 (-> connection1 (.flushAll))
                 (create-model-atom "user" "federico")
                 (generate-helper-funcs "user" "federico" user-federico)
                 (user-federico-add "friends" "jacinto")
                 (expect (= "jacinto" (-> connection1 (.spop "user:federico:friends"))))
                 (expect (= #{"jacinto"} (:friends @user-federico)))))

(describe "when  adding 2 friends to a member of that model"
          (do-it "should store the objects in memory and in redis"
                 (defredismodel "user" {:attributes ["name"] :collections ["friends"]})
                 (def pool1 (configure-connection {}))
                 (def connection1 (.getResource pool1))
                 (-> connection1 (.flushAll))
                 (create-model-atom "user" "federico")
                 (generate-helper-funcs "user" "federico" user-federico)
                 (user-federico-add "friends" "jacinto")
                 (user-federico-add "friends" "bernardo")
                 (expect (= #{ "jacinto", "bernardo"} (:friends @user-federico)))))
