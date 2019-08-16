[![Join our Discord server](https://discordapp.com/api/guilds/547121273684099102/widget.png?style=shield)](https://discordapp.com/invite/FHTtyT5)
[![Build Status](https://travis-ci.com/jakeryang/SimplePaintball.svg?branch=master)](https://travis-ci.com/jakeryang/SimplePaintball)

# SimplePaintball
Spigot 1.14 Paintball Plugin. Made with the idea that the Minecraft accounts are shared.

Compatible with 1.13 - 1.14.2

### Setup
1. Have an arena map where players can't get out
2. Make an arena: `/pb create <title>`
3. Add blue spawns: `/pb set blue`
4. Add red spawns: `/pb set red`
5. Add end spawn (where players teleport at the end of the game): `/pb set end`
6. Add lobby spawn (where players wait for the game to start): `/pb set lobby`
7. Activate arena: `/pb set activate`
8. Add sign:
   1. pb join
   2. <title>
9. Enjoy!

### The Game
When a player joins a game, they are teleported to the lobby and placed in adventure mode with a few objects in their hot bar: a leave bed 
and wool blocks. Players can right click on those blocks in order to either leave the game or to choose a weapon to use when the game starts. When the game is about to start, players are randomly placed on to different teams (red or blue). The players play the game for the most kills, the team with the most kills is announced after the game is over.

### The Weapons
* Sniper : Shoots straight, no drop
* Rocket Launcher : On impact, multiple snowballs fire out for area damage
* Shotgun : 3 snowballs fire out
* Minigun : Highfire rate with little accuracy
* Admin : Firerate of minigun with accuracy of sniper. Lightning strikes when a player is in the arena. Player holding the gun does not 
have to be part of arena

## Building
This repository utlizes Maven to build the JAR file. In Eclipse the process is simple: Right-click on your project -> `Run As...` -> `Maven Build`. A window will pop up to add some options for building. In the `Goals` field, type in `clean install`. Then click on `Apply` then on `Run`. This will will download the dependencies and build a jar file that will output in the `/target` folder. Place this in plugins and you're good to go!

### Note
In the pom.xml file, there's a section where it copies the built jar files to a certain directories (the plugin directory of my dev servers). These options are for me, but you can change them anyway you want. You might get an error if the path specified doesn't exist on your computer.

## API
As of v0.3.3-alpha.2, an API has been tested and created to add additional weapons to the game. If you would like to know how to add custom weapons look at the example repo: https://github.com/jakeryang/SimpleGun. 

### Note
You can replace any of the existing weapons by giving it the same name. Through the `getName()` method of the `Gun` class, which you extend to create a custom weapon, give it an existing name, such as `RocketLauncher` and it will replace the existing Rocket Launcher.

