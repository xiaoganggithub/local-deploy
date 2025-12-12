package zg.yoyo.localdeploy.application;

import zg.yoyo.localdeploy.infrastructure.emqx.EmqxPublisher;
import zg.yoyo.localdeploy.infrastructure.emqx.EmqxSubscriber;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Component
@RequiredArgsConstructor
@Slf4j
public class EmqxDemoApplication implements CommandLineRunner {

    private final EmqxPublisher emqxPublisher;
    private final EmqxSubscriber emqxSubscriber;

    @Override
    public void run(String... args) throws Exception {
        log.info("🚀 Starting EMQX Message Broker Demo Application");
        
        try {
            // Step 1: Connect publisher and subscriber
            log.info("🔌 Connecting to EMQX broker...");
            emqxSubscriber.connect();
            emqxPublisher.connect();
            
            // Wait a moment for connections to establish
            TimeUnit.SECONDS.sleep(2);
            
            // Step 2: Subscribe to topic
            log.info("📡 Subscribing to topic...");
            emqxSubscriber.subscribe();
            
            // Step 3: Publish test messages
            log.info("📤 Publishing test messages...");
            
            // Publish 5 test messages with delay
            for (int i = 1; i <= 5; i++) {
                String message = String.format("Hello EMQX! This is test message #%d from publisher", i);
                emqxPublisher.publish(message);
                TimeUnit.SECONDS.sleep(1); // Delay between messages
            }
            
            // Step 4: Publish to a different topic
            String customTopic = "emqx/custom-topic";
            log.info("📤 Publishing message to custom topic: {}", customTopic);
            emqxSubscriber.subscribe(customTopic, 1); // Subscribe to custom topic
            emqxPublisher.publish(customTopic, "This is a message to a custom topic", 1);
            
            // Wait for messages to be processed
            TimeUnit.SECONDS.sleep(3);
            
            // Step 5: Demonstrate unsubscribe
            log.info("🔇 Unsubscribing from custom topic: {}", customTopic);
            emqxSubscriber.unsubscribe(customTopic);
            
            // Publish to unsubscribed topic (should not be received)
            emqxPublisher.publish(customTopic, "This message should NOT be received", 1);
            
            // Step 6: Final test message
            emqxPublisher.publish("Final test message before disconnect");
            
            // Wait a moment
            TimeUnit.SECONDS.sleep(2);
            
            // Step 7: Disconnect
            log.info("🔌 Disconnecting from EMQX broker...");
            emqxSubscriber.disconnect();
            emqxPublisher.disconnect();
            
            log.info("🎉 EMQX Demo Application completed successfully!");
            
        } catch (Exception e) {
            log.error("❌ EMQX Demo Application failed: {}", e.getMessage(), e);
            throw e;
        }
    }
}