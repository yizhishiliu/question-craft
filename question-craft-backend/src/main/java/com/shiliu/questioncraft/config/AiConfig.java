package com.shiliu.questioncraft.config;

import com.zhipu.oapi.ClientV4;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author <a href="https://github.com/yizhishiliu">一之十六</a>
 *
 * 智谱AI配置类
 */
@Configuration
@ConfigurationProperties(prefix = "ai")
@Data
public class AiConfig {

    /**
     * api key 从平台获取
     */
    private String apiKey;

    @Bean
    public ClientV4 getGlientV4() {
        return new ClientV4.Builder(apiKey).build();
    }
}
