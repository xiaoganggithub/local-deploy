package zg.yoyo.localdeploy.infrastructure.config;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Configuration;

/**
 * @author zhenggang
 */
@Configuration
@MapperScan("zg.yoyo.localdeploy.infrastructure.persistence")
public class MybatisPlusConfig {
}
