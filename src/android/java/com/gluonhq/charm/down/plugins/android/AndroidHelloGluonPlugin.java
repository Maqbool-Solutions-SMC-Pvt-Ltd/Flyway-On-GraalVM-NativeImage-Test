package com.gluonhq.charm.down.plugins.android;

import android.content.res.AssetManager;
import com.gluonhq.charm.down.plugins.HelloGluonPlugin;
import javafx.util.Pair;
import javafxports.android.FXActivity;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class AndroidHelloGluonPlugin implements HelloGluonPlugin {

    private static final FXActivity ACTIVITY = FXActivity.getInstance();

    @Override
    public List<Pair<InputStream, String>> migrationList(String path) {
        List<Pair<InputStream, String>> migrationFiles = new ArrayList<>();
        AssetManager assetManager = ACTIVITY.getAssets();

        try {
            String[] files = assetManager.list(path);
            if (files != null) {
                for (String file : files) {
                    InputStream inputStream = assetManager.open(path + "/" + file);
                    migrationFiles.add(new Pair<>(inputStream, file));
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return migrationFiles;
    }
}