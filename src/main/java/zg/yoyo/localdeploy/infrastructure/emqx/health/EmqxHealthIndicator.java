package zg.yoyo.localdeploy.infrastructure.emqx.health;

import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import zg.yoyo.localdeploy.infrastructure.emqx.core.EmqxTemplate;

public class EmqxHealthIndicator implements HealthIndicator {

    private final EmqxTemplate template;

    public EmqxHealthIndicator(EmqxTemplate template) {
        this.template = template;
    }

    @Override
    public Health health() {
        boolean ok = template.isConnected();
        if (ok) {
            return Health.up().build();
        } else {
            return Health.down().withDetail("emqx", "disconnected").build();
        }
    }
}
