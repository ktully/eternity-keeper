A Pillars of Eternity save editor and character importer/exporter.

# Dependencies
Several dependencies are already bundled with the project. In order to build and run the project you will also need the following:

* [Apache Maven](https://maven.apache.org/)
* [JDK 1.8](http://www.oracle.com/technetwork/java/javase/downloads/jdk8-downloads-2133151.html) or above

# Support
Currently this has only been tested on Windows. Mac OSX and Linux support, however, is currently blocked by issues with the Chromium Embedded Framework.

# Contributing
1. Fork this repository.
2. Clone your forked repository.
3. Make changes.
4. Write tests.
5. Submit a pull request.

# Building
This project uses maven so building it should be relatively straightforward.

	mvn install

# Running
Assuming you've already built the project and have a `target` directory now and have your Java 8 executable in your `PATH`, you can use `run.bat` to run the project.

# Acknowledgements
The icon used by Eternity Keeper was created by [Alexander Loginov](http://alexanderloginov.deviantart.com/).

Pillars of Eternity uses the [SharpSerializer](http://www.sharpserializer.com/) library to serialize its saved game data. The [SharpSerializer](http://www.sharpserializer.com/) source code was therefore referenced heavily in the creation of the serialization implementation however the implementation cannot be considered a complete port as it is coupled tightly with the save game format and cannot function as a serializer/deserializer without it.
