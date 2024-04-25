package com.gluonhq.charm.down.plugins;

import com.gluonhq.charm.down.DefaultServiceFactory;

public class FlywayContextServiceFactory extends DefaultServiceFactory<FlywayContextService> {

    public FlywayContextServiceFactory() {
        super(FlywayContextService.class);
    }
}
