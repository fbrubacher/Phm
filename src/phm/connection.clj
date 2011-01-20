(ns phm.connection
  (:import org.apache.commons.pool.impl.GenericObjectPool$Config)
  (:import redis.clients.jedis.JedisPool))

(defn- add-prefix [prefix]
  (fn [key] (str prefix key)))

(def ^{:private true} default-options
  {:host "localhost"
   :port 6379})

(defn pool-config
  "Create a default pool"
  [options]
  (org.apache.commons.pool.impl.GenericObjectPool$Config.))

(defn configure-connection
  ([options]
     (let [{:keys (host port)} (merge default-options)]
       (JedisPool. (pool-config options) host port))))

(defn with-connection
  "Get a connection from the given pool and run the function f passing the connection.
  Finally, return the connection resource to the pool"
  [pool f]
  (let [connection (.getResource pool)]
    (try
      (f connection)
      (finally (when connection
                 (.returnResource pool connection))))))
