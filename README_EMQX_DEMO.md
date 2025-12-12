# EMQX Message Broker Demo Application

## Overview

This is a comprehensive demonstration application for the EMQX message broker platform, showcasing complete message publishing and subscription functionality with proper connection management and error handling.

## Features

- **Message Publishing**: Reliable message publishing with configurable QoS levels
- **Message Subscription**: Real-time message listening with topic management
- **Connection Management**: Automatic reconnection, connection state tracking
- **Error Handling**: Comprehensive error handling and logging
- **Multi-topic Support**: Subscribe to multiple topics simultaneously
- **Customizable Configuration**: Easy configuration via application.yml
- **Clear Logging**: Structured logging with status indicators

## Architecture

The demo follows a layered architecture:

```
┌─────────────────────────────────────────────────────────┐
│                    Application Layer                    │
│ ┌─────────────────────────────────────────────────────┐ │
│ │                EmqxDemoApplication                  │ │
│ │  - Main demo orchestration                          │ │
│ │  - Step-by-step demonstration flow                  │ │
│ └─────────────────────────────────────────────────────┘ │
├─────────────────────────────────────────────────────────┤
│                   Infrastructure Layer                 │
│ ┌─────────────────────────────────────────────────────┐ │
│ │                      EMQX Package                   │ │
│ │  ┌─────────────────┐  ┌───────────────────────────┐ │ │
│ │  │  EmqxPublisher  │  │   EmqxSubscriber         │ │ │
│ │  │  - Publish      │  │   - Subscribe            │ │ │
│ │  │  - Connect      │  │   - Message listening    │ │ │
│ │  │  - Disconnect   │  │   - Topic management     │ │ │
│ │  └─────────────────┘  └───────────────────────────┘ │ │
│ │  ┌─────────────────────────────────────────────────┐ │ │
│ │  │          EmqxCallbackHandler                    │ │ │
│ │  │  - Connection lost handling                     │ │ │
│ │  │  - Message delivery tracking                    │ │ │
│ │  └─────────────────────────────────────────────────┘ │ │
│ └─────────────────────────────────────────────────────┘ │
│ ┌─────────────────────────────────────────────────────┐ │
│ │                     Config Package                  │ │
│ │  ┌─────────────────────────────────────────────────┐ │ │
│ │  │                 EmqxConfig                      │ │ │
│ │  │  - EMQX broker configuration                    │ │ │
│ │  │  - Client settings                              │ │ │
│ │  └─────────────────────────────────────────────────┘ │ │
│ └─────────────────────────────────────────────────────┘ │
└─────────────────────────────────────────────────────────┘
```

## Configuration

### Prerequisites

- Java 17 or higher
- Maven 3.6 or higher
- EMQX broker running on `tcp://127.0.0.1:1883` (default)

### Application Configuration

Edit `src/main/resources/application.yml` to configure EMQX settings:

```yaml
emqx:
  broker: tcp://127.0.0.1:1883  # EMQX broker address
  client-id: emqx-demo-client     # Client identifier prefix
  username: admin                 # EMQX username
  password: public                # EMQX password
  topic: emqx/demo               # Default topic
  qos: 1                         # Default QoS level (0, 1, 2)

logging:
  level:
    zg.yoyo.localdeploy: debug   # Enable debug logging for the demo
    org.eclipse.paho.client.mqttv3: debug  # EMQX client debug logging
```

## Dependencies

The demo uses the following dependencies:

- **Spring Boot 3.3.5**: Application framework
- **Eclipse Paho MQTT Client 1.2.5**: EMQX client library
- **Lombok**: Boilerplate code reduction

Dependencies are managed via Maven in `pom.xml`.

## Startup Steps

### 1. Start EMQX Broker

Ensure the EMQX broker is running on the configured address. You can start EMQX using Docker:

```bash
docker run -d --name emqx -p 1883:1883 -p 8081:8081 -p 8083:8083 -p 8883:8883 -p 8084:8084 -p 18083:18083 emqx/emqx:latest
```

### 2. Build the Application

```bash
mvn clean package -DskipTests
```

### 3. Run the Application

```bash
java -jar target/local-deploy-0.0.1-SNAPSHOT.jar
```

Or run directly from IDE:
- Import the project as a Maven project
- Run the main Spring Boot application class: `zg.yoyo.localdeploy.LocalDeployApplication`
- The EMQX demo will execute automatically as a CommandLineRunner

## Verification Methods

### 1. Monitor Log Output

The application provides clear log messages with status indicators:

```
🚀 Starting EMQX Message Broker Demo Application
🔌 Connecting to EMQX broker...
EMQX subscriber connected to broker: tcp://127.0.0.1:1883
EMQX publisher connected to broker: tcp://127.0.0.1:1883
📡 Subscribing to topic...
Subscribed to topic: emqx/demo with QoS: 1
📤 Publishing test messages...
Message published successfully. Topic: emqx/demo, QoS: 1, Message: Hello EMQX! This is test message #1 from publisher
📩 Received message: Topic=emqx/demo, QoS=1, Message=Hello EMQX! This is test message #1 from publisher
...
🎉 EMQX Demo Application completed successfully!
```

### 2. EMQX Dashboard

Access the EMQX dashboard at `http://localhost:18083` (default credentials: admin/public) to monitor:

- Connected clients
- Message statistics
- Topic subscriptions
- Message flow

### 3. Key Demo Steps Verification

The demo executes the following steps sequentially:

| Step | Action | Expected Result |
|------|--------|-----------------|
| 1 | Connect publisher and subscriber | Successful connection messages |
| 2 | Subscribe to default topic | Subscription confirmation message |
| 3 | Publish 5 test messages | 5 published messages with corresponding received messages |
| 4 | Subscribe to custom topic | Custom topic subscription message |
| 5 | Publish to custom topic | Message published and received on custom topic |
| 6 | Unsubscribe from custom topic | Unsubscribe confirmation message |
| 7 | Publish to unsubscribed topic | Message published but NOT received |
| 8 | Final test message | Message published and received on default topic |
| 9 | Disconnect clients | Disconnection messages |

## Code Structure

### Core Components

1. **EmqxDemoApplication.java**
   - Main demo orchestration
   - Step-by-step demonstration flow

2. **EmqxPublisher.java**
   - Message publishing functionality
   - Connection management
   - Error handling

3. **EmqxSubscriber.java**
   - Message subscription and listening
   - Topic management
   - Real-time message processing

4. **EmqxCallbackHandler.java**
   - Connection state monitoring
   - Message delivery tracking
   - Error handling callbacks

5. **EmqxConfig.java**
   - EMQX broker configuration
   - MQTT client settings
   - Spring Bean configuration

## Customization

### Publishing Messages

```java
// Publish to default topic
emqxPublisher.publish("Hello World");

// Publish to custom topic with QoS 2
emqxPublisher.publish("custom/topic", "Important message", 2);
```

### Subscribing to Topics

```java
// Subscribe to default topic
emqxSubscriber.subscribe();

// Subscribe to custom topic with QoS 1
emqxSubscriber.subscribe("custom/topic", 1);

// Subscribe to multiple topics
String[] topics = {"topic1", "topic2", "topic3"};
int[] qos = {0, 1, 2};
emqxSubscriber.subscribe(topics, qos);
```

### Unsubscribing

```java
// Unsubscribe from default topic
emqxSubscriber.unsubscribe();

// Unsubscribe from custom topic
emqxSubscriber.unsubscribe("custom/topic");
```

## Troubleshooting

### Common Issues

1. **Connection Failed: Connection refused**
   - Ensure EMQX broker is running
   - Check broker address and port in application.yml
   - Verify network connectivity

2. **Authentication Failed: Bad user name or password**
   - Check EMQX username and password in application.yml
   - Verify credentials exist in EMQX

3. **Messages Not Received**
   - Check if subscriber is connected
   - Verify topic names match between publisher and subscriber
   - Check QoS configuration
   - Review log messages for errors

4. **Automatic Reconnection Not Working**
   - Verify `automaticReconnect` is enabled in EmqxConfig
   - Check connection timeout and keepalive settings

### Log Analysis

Check the application logs for:
- Connection status messages
- Error messages with stack traces
- Message publishing and receiving events
- Topic subscription changes

## Performance Considerations

- **QoS Levels**: Higher QoS levels provide more reliability but use more network resources
- **Message Size**: Keep messages reasonably sized for optimal performance
- **Connection Pooling**: For high-throughput applications, consider connection pooling
- **Topic Design**: Use hierarchical topic structures for efficient filtering

## License

This demo application is provided for educational purposes.

## Support

For issues or questions:

1. Check the EMQX documentation: https://docs.emqx.io/
2. Review the Eclipse Paho documentation: https://www.eclipse.org/paho/
3. Examine the application logs for error details
4. Verify EMQX broker status and configuration

---

**Enjoy exploring EMQX message broker functionality with this demo application!** 🚀
