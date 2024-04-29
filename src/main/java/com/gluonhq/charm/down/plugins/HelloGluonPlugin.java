package com.gluonhq.charm.down.plugins;

import javafx.util.Pair;

import java.io.InputStream;
import java.util.List;

public interface HelloGluonPlugin {

    default List<Pair<InputStream, String>> migrationList(String dbFolder) {
        return null;
    }

}
