package org.shoulder.monitor;

import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;

public class MockHealthIndicator implements HealthIndicator {

    private final Health health;

    public MockHealthIndicator(Health health) {
        this.health = health;
    }

    @Override
    public Health health() {
        return health;
    }
}
