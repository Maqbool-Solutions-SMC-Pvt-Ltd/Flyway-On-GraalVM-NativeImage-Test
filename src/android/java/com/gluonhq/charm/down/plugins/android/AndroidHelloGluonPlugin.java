package com.gluonhq.charm.down.plugins.android;

import android.content.Intent;
import android.net.Uri;
import android.support.v4.provider.DocumentFile;
import com.gluonhq.charm.down.HelloGluonPlugin;
import com.maqboolsolutions.flywaygraalvmtest.Main;
import javafxports.android.FXActivity;
import java.io.*;
import java.nio.file.Files;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import static android.app.Activity.RESULT_OK;

public class AndroidHelloGluonPlugin implements HelloGluonPlugin {

    private static final FXActivity ACTIVITY = FXActivity.getInstance();
    private Connection connection;
    private static final Logger LOG = Logger.getLogger(AndroidHelloGluonPlugin.class.getName());

    @Override
    public void saveToHsqldb() {

    }
}