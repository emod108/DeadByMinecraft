Dead by Minecraft

This is a Minecraft server plugin, which recreates game mechanics of Dead by Daylight in Minecraft, made using Spigot API and Maven.
This is a demo version, which lacks a lot of features, has glitches and perfomance issues, and also requires manual usage of commands to start a game.

Map creation tool is not very intuitive, hard to use, only allows to place certain objects.
Also MapData class has fields (object counters) which shouldn't be there, but I didn't remove them yet, because I'm not sure how to do it
without breaking loading of existing maps.

The plugin was created and tested for Spigot servers on version 1.20.2

Dependencies:
1) This plugin uses GlowingEntities util made by SkytAsul: https://github.com/SkytAsul/GlowingEntities/tree/master
(Copy-pasted into the project, GlowingEntities.java)
2) This plugin uses my own CrawlingPlugin, which is not public yet and requires some polishing. It will be added later.

There will be future updates to this plugin later soon, right now it can be used only for educational purposes.