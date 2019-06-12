[![Join our Discord server](https://discordapp.com/api/guilds/547121273684099102/widget.png?style=shield)](https://discordapp.com/invite/FHTtyT5)

# SimplePaintball
Spigot 1.14 Paintball Plugin. Made with the idea that the Minecraft accounts are shared.

Compatible with 1.13 - 1.14.2

## Setup
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

## The Game
When a player joins a game, they are teleported to the lobby and placed in adventure mode with a few objects in their hot bar: a leave bed 
and wool blocks. Players can right click on those blocks in order to either leave the game or to choose a weapon to use when the game starts. When the game is about to start, players are randomly placed on to different teams (red or blue). The players play the game for the most kills, the team with the most kills is announced after the game is over.

## The Weapons
* Sniper : Shoots straight, no drop
* Rocket Launcher : On impact, multiple snowballs fire out for area damage
* Shotgun : 3 snowballs fire out
* Minigun : Highfire rate with little accuracy
* Admin : Firerate of minigun with accuracy of sniper. Lightning strikes when a player is in the arena. Player holding the gun does not 
have to be part of arena

In the future, an API will be introduced for people to be able to make custom weapons and be able to use them.

