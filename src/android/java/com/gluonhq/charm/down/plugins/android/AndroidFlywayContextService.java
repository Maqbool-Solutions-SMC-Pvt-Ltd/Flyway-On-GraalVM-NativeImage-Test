package com.gluonhq.charm.down.plugins.android;

import com.gluonhq.charm.down.plugins.FlywayContextService;

import org.flywaydb.core.api.android.ContextHolder;

import javafxports.android.FXActivity;

public class AndroidFlywayContextService implements FlywayContextService {

    @Override
    public void setContext() {
        ContextHolder.setContext(FXActivity.getInstance());
    }

}
