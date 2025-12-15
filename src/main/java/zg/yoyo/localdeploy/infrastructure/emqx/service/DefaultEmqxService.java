package zg.yoyo.localdeploy.infrastructure.emqx.service;

import java.util.concurrent.CompletableFuture;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.stereotype.Service;
import zg.yoyo.localdeploy.infrastructure.emqx.core.EmqxTemplate;

/**
 * @author zhenggang
 */
@Service
@RequiredArgsConstructor
@ConditionalOnBean(EmqxTemplate.class)
public class DefaultEmqxService implements EmqxService {

    private final EmqxTemplate template;

    @Override
    public void send(String topic, String payload, int qos) throws Exception {
        template.publish(topic, payload, qos);
    }

    @Override
    public CompletableFuture<Void> sendAsync(String topic, String payload, int qos) {
        return template.publishAsync(topic, payload, qos);
    }

    @Override
    public void subscribe(String topic, int qos) throws Exception {
        template.subscribe(topic, qos, (t, m) -> {});
    }

    @Override
    public void unsubscribe(String topic) throws Exception {
        template.unsubscribe(topic);
    }

    @Override
    public boolean isConnected() {
        return template.isConnected();
    }
}
