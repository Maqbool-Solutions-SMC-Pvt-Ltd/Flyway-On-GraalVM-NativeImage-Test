package com.gluonhq.charm.down;

import javafx.util.Pair;

import java.io.InputStream;
import java.util.List;

public interface HelloGluonPlugin {

    public void saveMigrations();

    default List<Pair<InputStream, String>> migrationList(String dbFolder) {
        return null;
    }

}
