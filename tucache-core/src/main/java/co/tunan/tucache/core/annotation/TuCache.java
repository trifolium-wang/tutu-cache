package co.tunan.tucache.core.annotation;

import co.tunan.tucache.core.aspect.TuCacheAspect;
import org.springframework.core.annotation.AliasFor;

import java.lang.annotation.*;
import java.util.concurrent.TimeUnit;

/**
 * Created by wangxudong on 2020/04/09.
 * <p>
 * 该注解用于缓存方法返回的数据，key支持spel表达式
 * 可指定缓存过期时间和redis文档目录
 * <p>
 * 如果从缓存中拿不到或者出现超时，异常等情况则从方法中拿
 *
 * @version: 1.0
 * @modified :
 * @see TuCacheAspect
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Documented
public @interface TuCache {

    /**
     * Note on the method, cache the return value of the method, the key is the key of redis, and the value is json
     * Expire is the cache expiration time, when the cache is re-stored, the cache time will be reset
     * If resetExpire is set to false, if the cache exists in the cache, the data will be updated directly without resetting the time.
     * condition, resetExpire temporarily unavailable
     * <p>
     * 注解在方法上，将方法的返回值进行缓存，key为redis的key，值为json
     * expire为缓存过期时间，当缓存重新存入时，则缓存时间会重置
     * 如果设置resetExpire为false的情况下，如果缓存中存在该缓存，则直接更新数据而不会重置时间。
     * condition, resetExpire 暂时无法使用
     */

    @AliasFor("key")
    String value() default "";

    /**
     * 缓存超时时间
     * Cache timeout time, in seconds
     */
    long expire() default -1;

    /**
     * 时间单位，默认为秒
     *
     * @return
     */
    TimeUnit timeUnit() default TimeUnit.SECONDS;

    @AliasFor("value")
    String key() default "";

    /**
     * Whether to reset the time every time the cache is hit
     * 是否每次命中缓存则重置时间
     */
    boolean resetExpire() default false;

    /**
     * 条件表达式
     * 使用spEL表达式来告诉TuCache是否需要生效
     * spEl表达式必须返回boolean
     */
    String condition() default "true";
}

