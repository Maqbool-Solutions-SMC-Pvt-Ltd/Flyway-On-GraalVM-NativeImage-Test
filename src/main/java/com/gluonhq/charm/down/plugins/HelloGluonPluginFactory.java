package com.gluonhq.charm.down.plugins;

import com.gluonhq.charm.down.DefaultServiceFactory;

public class HelloGluonPluginFactory extends DefaultServiceFactory<HelloGluonPlugin> {
    public HelloGluonPluginFactory() {
        super(HelloGluonPlugin.class);
    }

}
