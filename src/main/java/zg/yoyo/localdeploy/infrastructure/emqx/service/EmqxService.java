package zg.yoyo.localdeploy.infrastructure.emqx.service;

import java.util.concurrent.CompletableFuture;

/**
 * @author zhenggang
 */
public interface EmqxService {
    void send(String topic, String payload, int qos) throws Exception;
    CompletableFuture<Void> sendAsync(String topic, String payload, int qos);
    void subscribe(String topic, int qos) throws Exception;
    void unsubscribe(String topic) throws Exception;
    boolean isConnected();
}
