(ns ^{:doc "simple SHA-1 implementation written in pure clojure"
      :author "Joshua Greenberg"}
  joshua-g.sha-1)


(defn sha-1 [byte-coll]
  {:pre [(coll? byte-coll)
         (every? integer? byte-coll)]}
  (let [
        int-to-big-endian-k-bytes
        (fn [k num]
          (->> [['() num] (range k)]
               (apply reduce
                      (fn [[bs n] _]
                        [(cons (mod n 256) bs) (quot n 256)]))
               (first)))

        big-endian-bytes-to-int
        (fn [bs]
          (->> [0 bs]
               (apply reduce (fn [x b] (+ (* 256 x) b)))))

        trunc-to-32-bits (partial bit-and 0xFFFFFFFF)

        leftrotate
        (fn [n x]
          (trunc-to-32-bits
           (bit-or (bit-shift-left x n)
                   (bit-shift-right x (- 32 n)))))

        expand-chunk
        (fn [chunk-16]
          (->> [(vec chunk-16) (range 16 80)]
               (apply reduce
                      (fn [chk i]
                        (->> (bit-xor (chk (- i 3))
                                      (chk (- i 8))
                                      (chk (- i 14))
                                      (chk (- i 16)))
                             (leftrotate 1)
                             (conj chk))))))

        bcd-transform
        (fn [i b c d]
          (cond
           (<= 0 i 19)  [(bit-or (bit-and b c)
                                 (bit-and (bit-not b) d))
                         0x5A827999]
           (<= 20 i 39) [(bit-xor b c d)
                         0x6ED9EBA1]
           (<= 40 i 59) [(bit-or (bit-and b c)
                                 (bit-and c d)
                                 (bit-and d b))
                         0x8F1BBCDC]
           :else        [(bit-xor b c d)
                         0xCA62C1D6]))

        compress
        (fn [state next-chunk]
          (->> [state (range 0 80)]
               (apply reduce
                      (fn [[a b c d e] i]
                        (let [[f k] (bcd-transform i b c d)
                              temp (trunc-to-32-bits
                                    (+ (leftrotate 5 a) f e k (next-chunk i)))]
                          [temp a (leftrotate 30 b) c d])))
               (map + state)
               (map trunc-to-32-bits)))

        byte-count (count byte-coll)
        bit-count (* 8 byte-count)
        padding-zero-count (mod (- 55 byte-count) 64)
        chunks (->> byte-coll
                    (map (partial bit-and 0xFF))
                    ((fn [bs]
                       (concat bs
                               [0x80]
                               (repeat padding-zero-count 0x00)
                               (int-to-big-endian-k-bytes 8 bit-count))))
                    (partition 4)
                    (map big-endian-bytes-to-int)
                    (partition 16)
                    (map expand-chunk))

        init-state [0x67452301 0xEFCDAB89 0x98BADCFE 0x10325476 0xC3D2E1F0]
        ]
    (->> (reduce compress init-state chunks)
         (mapcat (partial int-to-big-endian-k-bytes 4)))))
