A Pillars of Eternity save editor and character importer/exporter.

# N.B.
This is a clone of the code from https://bitbucket.org/Fyorl/eternity-keeper/src/master/ but with the commit history revised to strip symbols from all the linux shared libraries (otherwise they were too large for github)

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

# Converting Windows Store save files to Steam/GoG
Saves from the Windows Store/GamePass game build cannot be loaded by Steam or GoG game builds.

The 0.4 alpha project release includes initial conversion code, allowing you to generate Steam/GoG compatible saves from Windows saves.

1. Run Eternity Keeper as Normal
1. Close the settings dialog if it appears
1. Enter the path to your Windows Store saves in 'Save Folder' box and click 'Search' to refresh the savegame list.
    * Typically your Windows Store saves are in a numeric subdirectory like `%USERPROFILE%\Saved Games\Pillars of Eternity\1234567890987654`.
1. Click on a Windows Store save file to load it
1. Progress indicator will spin as file is opened and converted - this may take a few minutes
1. When conversion has completed, the save file list will refresh showing a new `( converted)` save
1. Quit Enternity Keeper
1. Open the save directory in Windows Explorer and move the converted savefile into `%USERPROFILE%\Saved Games\Pillars of Eternity\`, so that the Steam/GoG version of the game can find it.

**Note:** Keep a backup of your original unconverted save file(s) - the conversion process could conceivably cause the game to crash or have other unanticipated bugs later.


# Features

* Auto updater (currently broken)
* Windows and Linux support
* Modify character attributes
* Modify all raw numeric character variables
* Modify character names (including companions)

**Note about the Raw tab**: The 'Raw' tab is basically a dump of all a character's stats from the save file. Changing most of the values here has not been tested and could result in a corrupt save file. Eternity Keeper will never overwrite your save files, it will just create a new, edited one, so you don't need to worry too much.

The values in the other tabs have been tested and will be accepted by the game. The plan is to fill out these other tabs with more stats once they have been tested. Things in the Raw tab such as 'BaseWill', etc. in which it is pretty obvious what they do, should be quite safe to modify.

# Planned Features
* Faster conversion from Windows to Steam format
* Mac support
* Character importing/exporting
* Party management
* Bring dead characters back to life
* Clean up vendors (i.e. delete all the crap you sold to vendors from the save files to make them smaller and faster to save in future)
* Modify inventory and stash
* Modify skills and talents
* Modify culture and race
* Modify grimoires
* Item editor
* Stronghold editor
* Modify global variables
* Modify companion portraits (note that you can already do this by just renaming and shuffling around files in the game's portraits directory)

# Eternity Keeper as a platform
One of the long-term goals of this project will be to allow people to create and edit items, talents, quests, etc. using an intuitive UI and to save them in a format that allows them to be inserted into a saved game that Pillars of Eternity will recognise.

Some work was already done towards allowing characters to be imported and exported (just like in the IE games) but it turned out a bit more fiddly than initially imagined and was disabled for this release.

All of the data for the above is present in saved game files though so it should all be eventually possible.

# Technical Stuff
There's nothing stopping the code being built on Macs, however the JCEF dependencies are not pre-built and bundled with the source code in the repository yet so you will have to build those too.

# What I learned about save files
I figured some people might be interested in how the save files work since save/load times are a bit of an issue. I haven't delved too deeply but I made a few observations.

Firstly, most of main game data is serialized into binary using the SharpSerializer library. It's a free, open-source library and seems fairly mature though after having to essentially re-implement the whole library in Java, I'm not sure it's entirely as fast as it could be when it comes to serialization. Serialization might not be the bottleneck though.

Secondly, there is a lot of stuff stored in saved games. Every single item ever sold to a vendor is stored. I think containers and items dropped on the ground are also stored but I haven't looked into the area files yet. Speaking of area files, the fog of war for each area is saved and also probably a few other state variables. This means that, as the game progresses, you will add more and more area files to your save file.

The saved games are just compressed zip files. This revelation obviously isn't anything new and I think a lot of save files these days are similar. The only thing to note about this is that they are compressed very heavily, i.e. more than the standard LZMA compression settings. This will mean saving and loading will be slower as the (de)compression step is more intensive. Eternity Keeper only uses default settings and the save files it produces are a fair bit less compressed than the files the game produces. Again, whether this is an actual bottleneck in the save/load process is completely up for debate as the (de)compression time could well just be negligible.


# Testing
You may run the TestEnvironment tool to automatically copy a small number of saves as well as any necessary game data to a temporary directory on your filesystem and then modify your settings file to point to these. This should make UI testing a bit easier and less destructive. You can run the tool after building the whole project with:

	java -cp target/eternity-0.1.-shaded.jar uk.me.mantas.eternity.TestEnvironment <game location> <save location>

Where *<game location>* is the path to your Pillars of Eternity install and *<save location>* is the path to your save file directory.

You may also provide the `--help` flag to see advanced options.

# Acknowledgements
The icon used by Eternity Keeper was created by [Alexander Loginov](http://alexanderloginov.deviantart.com/).

Pillars of Eternity uses the [SharpSerializer](http://www.sharpserializer.com/) library to serialize its saved game data. The [SharpSerializer](http://www.sharpserializer.com/) source code was therefore referenced heavily in the creation of the serialization implementation however the implementation cannot be considered a complete port as it is coupled tightly with the save game format and cannot function as a serializer/deserializer without it.
