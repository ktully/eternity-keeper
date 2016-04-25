A Pillars of Eternity save editor and character importer/exporter.

# Dependencies
Several dependencies are already bundled with the project. In order to build and run the project you will also need the following:

* [Apache Maven](https://maven.apache.org/)
* [JDK 1.8](http://www.oracle.com/technetwork/java/javase/downloads/jdk8-downloads-2133151.html) or above

# Support
Currently there is support for Windows and Linux versions.

# Contributing
1. Fork this repository.
2. Clone your forked repository.
3. Make changes.
4. Write tests.
5. Submit a pull request.

# Building
This project uses maven so building it should be relatively straightforward.

	mvn install -P<platform>

Where *<platform>* is one of `win32`, `win64`, or `linux64`.

# Running
Assuming you've already built the project and have a `target` directory now and have your Java 8 executable in your `PATH`, you can use `run.bat` or `run.sh` to run the project.
**Note:** Some users have reported segmentation faults when attempting to run Eternity Keeper using the OpenJDK. If you experience the same issue, switching to the Oracle JDK may resolve it.

# Testing
You may run the TestEnvironment tool to automatically copy a small number of saves as well as any necessary game data to a temporary directory on your filesystem and then modify your settings file to point to these. This should make UI testing a bit easier and less destructive. You can run the tool after building the whole project with:

	java -cp target/eternity-0.1.-shaded.jar uk.me.mantas.eternity.TestEnvironment <game location> <save location>

Where *<game location>* is the path to your Pillars of Eternity install and *<save location>* is the path to your save file directory.

You may also provide the `--help` flag to see advanced options.

# Acknowledgements
The icon used by Eternity Keeper was created by [Alexander Loginov](http://alexanderloginov.deviantart.com/).

Pillars of Eternity uses the [SharpSerializer](http://www.sharpserializer.com/) library to serialize its saved game data. The [SharpSerializer](http://www.sharpserializer.com/) source code was therefore referenced heavily in the creation of the serialization implementation however the implementation cannot be considered a complete port as it is coupled tightly with the save game format and cannot function as a serializer/deserializer without it.