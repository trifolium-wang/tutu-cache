package co.tunan.tucache.core.cache;

import java.util.concurrent.TimeUnit;

/**
 * This interface stipulates some basic caching interfaces used by TuCache.
 * If users need to use their own caches,
 * implement this interface to replace the default redis cache
 *
 * @author : wangxudong
 * @date: 2020/08/28 10:48
 * @see
 */
public interface TuCacheService {

    /**
     * add cache and set time
     *
     * @param expire 超时时间
     */
    void set(String key, Object value, long expire, TimeUnit timeUnit);

    /**
     * add cache
     */
    void set(String key, Object value);

    /**
     * get the data in the buffer
     */
    <T> T get(String key, Class<T> clazz);

    /**
     * get the data in the buffer and set time
     */
    <T> T get(String key, Class<T> clazz, long expire, TimeUnit timeUnit);

    /**
     * delete cache
     */
    void delete(String key);

    /**
     * fuzzy delete
     */
    void deleteKeys(String key);
}
