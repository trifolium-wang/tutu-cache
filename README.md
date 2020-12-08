tutu-cache 是为了解决SpringCache缓存注解不够灵活的问题而做的缓存注解项目。
使用注解方式来代替@Cacheable和@CacheEvict等注解
### 引入tutu-cache
1. 在springBoot中的使用
    * 引入jar依赖包
        ```xml
           <dependency>
               <groupId>co.tunan.tucache</groupId>
               <artifactId>spring-boot-starter</artifactId>
               <version>1.0.0</version>
           </dependency>
        ```
    * 在Configure类中注册javaBean redisTemplate或者使用默认的redisTemplate，必须开启aspectj的aop功能(默认是开启的)
2. 在springMVC中的使用
    * 引入jar依赖包
        ```xml
        <dependency>
          <groupId>co.tunan.tucache</groupId>
          <artifactId>cache-core</artifactId>
          <version>1.0.0</version>
        </dependency>
        ```
    * 在applicationContent.xml中配置
        ```xml
        <!-- 如果使用的缓存是redis则注入默认实现的RedisCacheService -->
        <bean id="redisCacheService" class="co.tunan.tucache.core.cache.impl.RedisCacheService">
            <property name="redisTemplate" ref="redisTemplate" />
        </bean>
      
        <bean id="tuCacheBean" class="co.tunan.tucache.core.TuCacheBean">
            <property name="tuCacheService" ref="redisCacheService" />
        </bean>
      
        <tx:annotation-driven proxy-target-class="true"/>
        <!-- 使用cgLib代理 -->
        <!-- 注意项目中需要引入spring-aop和spring-data-redis的相关依赖 -->
        ``` 
### 使用tu-cache
1. 使用tu-cache对service中的方法返回的数据进行缓存
    ```java
    @TuCache("test_service:getList")
    public List<String> getList(){
        return Arrays.asList("tu","nan");
    }
    ```
1. 使用tu-cache删除缓存中的数据
    ```java
    @TuCacheClear("test_service:getList")
    public void delList(){
    }
    ```
3. @TuCache参数
    * `String key() default ""` 缓存的字符串格式key,支持spel表达式，默认值为方法签名
    * `long expire() default -1` 缓存的过期时间，单位(秒),默认永不过期.
    * `boolean resetExpire() default false` 每次获取数据是否重置过期时间.
    * 样例:
        ```java
        @TuCache(key="test_service:getList:#{#endStr}",expire = 10)
        public List<String> getList(String endStr){
            return Arrays.asList("tu","nan",endStr);
        }
      
        // 如果需要调用本地的方法
        @TuCache(key="test_service:getList:#{this.endStr()}",expire = 120)
        public List<String> getList(){
            return Arrays.asList("tu","nan",endStr());
        }
        public String endStr(){
          return "end";
        }
        ```
4. @TuCacheClear参数
    * `String[] key() default {}` 删除的key数组，支持spel表达式
    * `String[] keys() default {}` 模糊删除的缓存key数组,支持spel表达式,对应redis中**deleteKeys**("test_service:")
    * `boolean sync() default false` 支持异步删除，无需等待删除的结果
    * 样例:
        ```java
        @TuCacheClear(key={"test_service:itemDetail:#{#id}"})
        public void deleteItem(Long id){ 
        }
      
        // 如果需要调用本地的方法
        @TuCacheClear(keys={"test_service:itemList:","test_service:itemDetail:+#{#id}"}, sync = true)
        public void deleteItem(Long id){
        }
        ```
    * _注意key和keys的区别_
    
### 高级使用
* tutu-cache默认提供了 RedisTuCacheService,如果用户使用的缓存是redis自动使用该默认缓存服务。
* 用户使用其他缓存，则需要自定义TuCacheService，实现该接口并注入到TuCacheBean中
* 在SpringBoot中则在Configure类中配置相应的bean自动使用自定义的bean
* 如果用户需要每个缓存前面添加同意的keyPrefix，TuCacheBean的prefixKey参数
* springBoot中配置
    ```yaml
    tucache:
      enable: true
      profiles:
        cache-prefix: my_tu_key_test:
    ```
* springMVC中注入到TuCacheBean
    ```xml
    <bean id="tucacheProfiles" class="co.tunan.tucache.core.config.TuCacheProfiles">
        <property name="cachePrefix" value="test_tucache_prefixkey:" />
    </bean>
    <bean id="tuCacheBean" class="co.tunan.tucache.core.TuCacheBean">
        <property name="tuCacheService" ref="redisCacheService" />
        <property name="tuCacheProfiles" ref="tucacheProfiles" />
    </bean>
    ```
* 关于默认RedisTuCacheService的序列化问题，建议使用Json的方式序列化值,高效率的情况下可以使用java序列化
* 使用Json序列化配置样例如下:
    ```java
    @Bean(name = "redisTemplate")
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory redisConnectionFactory) {
        RedisTemplate<String, Object> redisTemplate = new RedisTemplate<>();
        redisTemplate.setKeySerializer(new StringRedisSerializer());
        redisTemplate.setHashKeySerializer(new StringRedisSerializer());
        redisTemplate.setHashValueSerializer(new GenericJackson2JsonRedisSerializer(createGenericObjectMapper()));
        redisTemplate.setValueSerializer(new GenericJackson2JsonRedisSerializer(createGenericObjectMapper()));

        redisTemplate.setConnectionFactory(redisConnectionFactory);

        return redisTemplate;
    }

    private ObjectMapper createGenericObjectMapper() {
        final ObjectMapper objectMapper = new ObjectMapper();

        objectMapper.setTimeZone(TimeZone.getTimeZone("GMT+8"));

        objectMapper.registerModule(new SimpleModule().addSerializer(new StdSerializer<NullValue>(NullValue.class) {
            private String classIdentifier;

            @Override
            public void serialize(NullValue value, JsonGenerator jgen, SerializerProvider provider)
                    throws IOException {
                classIdentifier = StringUtils.hasText(classIdentifier) ? classIdentifier : "@class";
                jgen.writeStartObject();
                jgen.writeStringField(classIdentifier, NullValue.class.getName());
                jgen.writeEndObject();
            }
        }));


        objectMapper.enableDefaultTyping(ObjectMapper.DefaultTyping.NON_FINAL, JsonTypeInfo.As.PROPERTY);

        return objectMapper;
    }
    ```