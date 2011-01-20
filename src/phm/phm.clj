(ns phm.phm
  (:use phm.connection))

(def pool (configure-connection {}))

(def connection "remove this in the future" (.getResource pool))

(defn nest
  "Concat all values together separated by :"
  [& values]
  (reduce str (map #(str % ":") values)))

(defmacro defredismodel
  "Define a new model"
  [name properties]
  `(def ~(symbol (str name)) (with-meta ~properties {:name (symbol ~name)
                                                     :item_nbr (atom 0)})))

(defprotocol RedisBase
  "Operations to happen on a Clj coll"
  (add [self key value])
  (delete [self key]))

(extend clojure.lang.PersistentHashSet
  RedisBase
  {:add (fn [self key value]
          (with-connection pool
            (fn [connection]
              (-> connection (.sadd key value)))))
   :delete (fn [self key]
             (println "delete"))})

(defn increment-model-count
  "When a new occurrence is created, this increments the model atom by one"
  [model]
  (swap! (:item_nbr (meta model) inc)))

(defn create-redis-key
  "Saves the occurrence into Redis"
  [model attributes]
  (doseq [key (keys attributes)]
    (-> connection (.set (str (:redis-key @model) (re-find #"\w+" (str key)))
                         (key attributes)))))

(defn add-to-relation [atom k v]
  "Function to be called by a coll RedisBase protocol, orchestrates adding the element to a coll and also persists the element to redis."
  (if-let [relation ((keyword k) @atom)]
    (do
      (swap! atom update-in [(keyword k)] conj v)
      (add relation (str (:redis-key @atom) k) v))
    (do
      (let [values (swap! atom merge {(keyword k) #{v}})]
        (add ((keyword k) values) (str (:redis-key @atom) k) v)))))

(defmacro generate-helper-funcs
  "Macro to generate helper fns adding with add, delete and possible more in the future"
  [model occurrence atom]
  `(do
     (generate-indiv-funcs ~model ~occurrence "add" [key# value#]
                           (add-to-relation ~atom key# value#))
     (generate-indiv-funcs ~model ~occurrence "remove" [key# value#]
                           (println "not yet implemented"))))

(defmacro generate-indiv-funcs
  [model occurrence action args & body]
  `(defn ~(symbol (str model "-" occurrence "-" action)) ~args
     ~@body))

(defn occurrence-key
  [parent-model-name name]
  (nest parent-model-name name))

(defmacro create-model-atom
  "Creates an occurrence based on a model"
  [model name]
  (let [model-atom `(atom {:redis-key (occurrence-key ~model ~name)})]
     `(def ~(symbol (str model "-" name))
           ~model-atom)))
