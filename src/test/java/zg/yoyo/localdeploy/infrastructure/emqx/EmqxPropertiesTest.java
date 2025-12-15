package zg.yoyo.localdeploy.infrastructure.emqx;

import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.Test;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.core.env.MapPropertySource;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.core.env.StandardEnvironment;
import zg.yoyo.localdeploy.infrastructure.emqx.config.EmqxProperties;

import java.util.Map;

public class EmqxPropertiesTest {
    @Test
    void bindProperties() {
        StandardEnvironment env = new StandardEnvironment();
        MutablePropertySources sources = env.getPropertySources();
        sources.addFirst(new MapPropertySource("test", Map.of(
                "spring.emqx.broker-url", "tcp://localhost:1883",
                "spring.emqx.username", "u",
                "spring.emqx.password", "p",
                "spring.emqx.client-id", "cid",
                "spring.emqx.qos", "2"
        )));
        EmqxProperties props = Binder.get(env).bind("spring.emqx", EmqxProperties.class).get();
        assertEquals("tcp://localhost:1883", props.getBrokerUrl());
        assertEquals("u", props.getUsername());
        assertEquals("p", props.getPassword());
        assertEquals("cid", props.getClientId());
        assertEquals(2, props.getQos());
    }
}
