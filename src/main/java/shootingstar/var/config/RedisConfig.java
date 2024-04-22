package shootingstar.var.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
public class RedisConfig {
    @Value("${spring.data.redis.host}")
    private String redisHost;
    @Value("${spring.data.redis.port}")
    private int redisPort1; // jwt 레디스 포트
    @Value("${spring.data.redis.port2}")
    private int redisPort2; // mail 레디스 포트
    @Value("${spring.data.redis.port3}")
    private int redisPort3; // loginList 레디스 포트
    @Value("${spring.data.redis.port4}")
    private int redisPort4;
    @Value("${spring.data.redis.password}")
    private String redisPassword;


    // JWT 레디스 빈 등록
    @Bean
    public RedisConnectionFactory jwtRedisConnectionFactory() {
        RedisStandaloneConfiguration redisConfiguration = new RedisStandaloneConfiguration();
        redisConfiguration.setHostName(redisHost);
        redisConfiguration.setPort(redisPort1);
        redisConfiguration.setPassword(redisPassword);
        return new LettuceConnectionFactory(redisConfiguration);
    }

    // mail 레디스 빈 등록
    @Bean
    public RedisConnectionFactory mailRedisConnectionFactory() {
        RedisStandaloneConfiguration redisConfiguration = new RedisStandaloneConfiguration();
        redisConfiguration.setHostName(redisHost);
        redisConfiguration.setPort(redisPort2);
        redisConfiguration.setPassword(redisPassword);
        return new LettuceConnectionFactory(redisConfiguration);
    }

    // 로그인 리스트 레디스 빈 등록
    @Bean
    public RedisConnectionFactory loginListRedisConnectionFactory() {
        RedisStandaloneConfiguration redisConfiguration = new RedisStandaloneConfiguration();
        redisConfiguration.setHostName(redisHost);
        redisConfiguration.setPort(redisPort3);
        redisConfiguration.setPassword(redisPassword);
        return new LettuceConnectionFactory(redisConfiguration);
    }

    @Bean
    public RedisConnectionFactory participatingAuctionRedisConnectionFactory() {
        RedisStandaloneConfiguration redisConfiguration = new RedisStandaloneConfiguration();
        redisConfiguration.setHostName(redisHost);
        redisConfiguration.setPort(redisPort4);
        redisConfiguration.setPassword(redisPassword);
        return new LettuceConnectionFactory(redisConfiguration);
    }

    // JWT 레디스 템플릿 등록
    @Bean
    public RedisTemplate<?, ?> jwtRedisTemplate() {
        RedisTemplate<?, ?> redisTemplate = new RedisTemplate<>();
        redisTemplate.setConnectionFactory(jwtRedisConnectionFactory());   //connection
        redisTemplate.setKeySerializer(new StringRedisSerializer());    // key
        redisTemplate.setValueSerializer(new StringRedisSerializer());  // value
        return redisTemplate;
    }

    // mail 레디스 템플릿 등록
    @Bean
    public RedisTemplate<?, ?> mailRedisTemplate() {
        RedisTemplate<?, ?> redisTemplate = new RedisTemplate<>();
        redisTemplate.setConnectionFactory(mailRedisConnectionFactory());   //connection
        redisTemplate.setKeySerializer(new StringRedisSerializer());    // key
        redisTemplate.setValueSerializer(new StringRedisSerializer());  // value
        return redisTemplate;
    }

    // 로그인 리스트 레디스 템플릿 등록
    @Bean
    public RedisTemplate<?, ?> loginListRedisTemplate() {
        RedisTemplate<?, ?> redisTemplate = new RedisTemplate<>();
        redisTemplate.setConnectionFactory(loginListRedisConnectionFactory());   //connection
        redisTemplate.setKeySerializer(new StringRedisSerializer());    // key
        redisTemplate.setValueSerializer(new StringRedisSerializer());  // value
        return redisTemplate;
    }

    @Bean
    public RedisTemplate<?, ?> participatingAuctionRedisTemplate() {
        RedisTemplate<?, ?> redisTemplate = new RedisTemplate<>();
        redisTemplate.setConnectionFactory(participatingAuctionRedisConnectionFactory());   //connection
        redisTemplate.setKeySerializer(new StringRedisSerializer());    // key
        redisTemplate.setValueSerializer(new StringRedisSerializer());  // value
        return redisTemplate;
    }

    // 레디스에 저장되는 데이터 JSON 타입으로 직렬화
    @Bean
    public RedisSerializer<Object> defaultRedisSerializer() {
        return new GenericJackson2JsonRedisSerializer();
    }

    @Bean
    public RedisTemplate<String, Object> chatRedisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);

        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        GenericJackson2JsonRedisSerializer jsonSerializer =
                new GenericJackson2JsonRedisSerializer(objectMapper);

        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(jsonSerializer);
        template.setHashKeySerializer(new StringRedisSerializer());
        template.setHashValueSerializer(jsonSerializer);
        template.afterPropertiesSet();
        return template;
    }
}
