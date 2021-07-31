# FlywayOnGraalVmNativeTest

A simple application with Java 11+, JavaFX 15+, GraalVM, FlywayDB & HSQLDB.

## Documentation

Read about such a sample [here](https://docs.gluonhq.com/#_hellofx). This project is a modified version for that's sample project with Flyway & HSQLDb implementation.

## Quick Instructions

We use [GluonFX plugin](https://docs.gluonhq.com/) to build a native image for platforms including desktop and android.
Please follow the prerequisites as stated [here](https://docs.gluonhq.com/#_requirements).

See [HSQLDB Sample Project](https://github.com/ctoabidmaqbool/HSQLDb-On-GraalVM-NativeImage-Test)

**Note:** Currently, only Desktop version of this sample if under focused.

### Desktop

Run the application on JVM/HotSpot:

    mvn gluonfx:run

Run the application and explore all scenarios to generate config files for the native image with:

    mvn gluonfx:runagent

Build a native image using:

    mvn gluonfx:build

Run the native image app:

    mvn gluonfx:nativerun


## Other Info:

Common log files:

    /target/gluonfx/log/

Windows log files:

    /target/gluonfx/x86_64-windows/gvm/log/

# Issues

- Flywaydb is not working with GraalVM native-image e.g. Migration files are not loading when running native .exe.
