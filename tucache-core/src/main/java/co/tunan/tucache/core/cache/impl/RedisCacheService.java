package co.tunan.tucache.core.cache.impl;

import co.tunan.tucache.core.cache.TuCacheService;
import org.springframework.data.redis.core.RedisTemplate;

import java.lang.reflect.Method;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * The default TuCacheService implementation class
 * Created by wangxudong on 2019/3/14.
 */
public class RedisCacheService implements TuCacheService {

    private RedisTemplate<String, Object> redisTemplate;

    private static final long NOT_EXPIRE = -1;

    @Override
    public void set(String key, Object value, long expire, TimeUnit timeUnit) {
        redisTemplate.opsForValue().set(key, value);
        if (expire != NOT_EXPIRE) {
            redisTemplate.expire(key, expire, timeUnit);
        }
    }

    @Override
    public void set(String key, Object value) {
        set(key, value, NOT_EXPIRE, null);
    }

    @Override
    public void delete(String key) {
        redisTemplate.delete(key);
    }

    @Override
    public void deleteKeys(String key) {
        Set<String> keys = redisTemplate.keys(key + "*");
        redisTemplate.delete(keys);
    }

    @Override
    public <T> T get(String key, Class<T> clazz) {
        return get(key, clazz, NOT_EXPIRE, null);
    }

    @Override
    public <T> T get(String key, Class<T> clazz, long expire, TimeUnit timeUnit) {
        Object value = redisTemplate.opsForValue().get(key);

        if (expire != NOT_EXPIRE) {
            redisTemplate.expire(key, expire, timeUnit);
        }

        if (value == null)
            return null;

        if (clazz.isPrimitive())
            return (T) value;

        try {
            if (value instanceof Number) {
                Method method = clazz.getMethod("valueOf", String.class);

                return clazz.cast(method.invoke("valueOf", value.toString()));
            }
        } catch (Exception e) {

            throw new RuntimeException(e.getMessage(), e);
        }

        return clazz.cast(value);

    }

    public void setRedisTemplate(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

}

