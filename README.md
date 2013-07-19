Hive13 IRC Bot
==============
Wikipage: http://wiki.hive13.org/Hive13_IRC_Bot

This is the bot that hanges out in irc.freenode.net #hive13.  The bot is written in Java with a MySQL database for logging messages and a PHP website for displaying said log.

Building
--------
Easiest way to build is using ant, the root of the project has an ant buildfile.xml which will generate a runnable jar file.

The bot is developed using eclipse, and if the repository is retrieved at the root it contains all libraries (other than the JDK) needed to build the bot.  I have tried to extensively comment the code to make everyone's lives easier. If you have already retrieved the project using Git, you can open it in Eclipse by going to File->Import->General->Existing Projects Into Workspace, then selecting the projects root directory.

Project Structure
-----------------
- /lib - Libraries the bot needs to run
- /res - Resource files associated with the bot, currently the MySQL database construction scripts and the PHP website.
- /src - Source code for the bot.
- /jIRCBot.properties - A properties file for basic configuration (Bot name, IRC Server, IRC Channel, password for connection, MySQL database names, etc,etc..)  This file also contains prototyped properties that have not been implemented yet, namely the ones related to the plugins.

Code Structure
--------------
This is all horribly out of date.  We recently migrated to using PircBotX 1.9.  This is the successor to the jibble PircBot framework.

*Old comments below*

The project is organized into two top level package trees, _org.hive13.jircbot_ and _org.jibble.pircbot_.  We are primarily interested in _org.hive13.jircbot_.  

### org.jibble.pircbot
The code for jIRCBot is an extension of the PIRCBot which provides the basic IRC bot framework that I have extended.  I have made some very minor changes to PIRCBot which is why its code is part of the project.

### org.hive13.jircbot
The main 'bot' code which starts the bot and directly inherits from the PircBot code is the jIRCBot.java class.  I sorta look at this class as the air traffic controller of the bot.  Requests come in from the wild world of the IRC chat room and this class directs them to sub-classes which handle these messages.

#### commands
These classes are what I consider the 'plugins' of the bot. jIB = java IRC Bot, not my most inspired chose for a prefix, I know.  At this point all of the commands are run asynchronously from the bot's primary thread.  There are two primary classes that all of the commands inherit from, `jIBCommand` and `jIBCommandThread`

* `jIBCommand`
    * The basic parent class for a new command.  This is designed for commands that will be run in response to user input. They will be activated in some manner by a user's action, run their task in a seperate thread, then exit.
* `jIBCommandThread`
    * This class for a command is a bit fancier.  These commands are designed to be long running asynchronous processes that will send alerts to the chat room based on external events.  The primary example of thse classes is the RssReader.  CommandThreads have a kind of "Loop" function that is executed at a set period, by default this period is 30 seconds.  The RssReader for example checks an RSS feed and then pushes any new updates to the chat room.

#### support
Pretty much what the name implies these are the classes which provide some helper functions to the rest of the bot.  These classes manage the properties and common tasks.

* `jIRCTools`
    * Common functions that many different classes use to Get Shit Done (R).  The class is divided into sections based on what functions are being provided be it database access, URL manipulation, or generating hashes.
* `jIRCProperties`
    * I try to break common settings out into a *.properties file, this class manages these properties.
