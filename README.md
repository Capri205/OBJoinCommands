# OBJoinCommands
Minecraft bukkit/spigot plugin to set a command to be run when a player joins a world or the server.

Useful for your lobby or game servers where you want something specific done when players join.<br />
Commands execute in order of sequence number, with 1 being the first.

Use the /objc command to add or remove a command for the server or a specific world.

Commands:<br />
/objc list [ server | &lt;world&gt; ] - to show current commands<br />
/objc add [ server | world &lt;worldname&gt; ] &lt;sequence&gt; &lt;command&gt; - set a command at a specific sequence for a world or the server<br />
/objc del [ server | world &lt;worldname&gt; ] &lt;sequence&gt; - remove a server or world command at a specific sequence<br />

Examples:

/objc add server 1 effect give @e glowing 200 - will cause all entities to glow for 200 seconds when a player joins the default world for the server.

Note that the sequence number will be 1 for the first command for that world or the server, regardless of what sequence number you provide. Subsequent
commands with sequence numbers higher by more than one will default to one more than the current sequence number. Sequence numbers can't have gaps.

/objc add world world_nether 1 give @r cooked_beef 10 - will give the joining player 10 steaks when joining the nether.

/objc list - will list all commands for the server and any worlds that have them registered.
/objc list world world_the_end - will list the commands for just The End world.

/objc del server 5 - will remove command at sequence number 5 if the command exists. Note that all commands above will move down in the
sequence order, so if there was a command with sequence number 6 then it will be sequence 5 following that command, and so on.

objc del world world 2 - will remove the command at sequence number 2 for the overworld.

objc set delay 400 - will set the global delay to 400 ticks or 20 seconds. Default value is 20 ticks or 1 second.

Currently any command that fails will fail silently but will be logged by the server.

Shorthands:<br />
The plugin will translate the @p and @s shorthands to the joining player, so it's not the nearest player or self as in regular commands.<br />
You can use other Minecraft shorthands in your commands, such as @r for random player, @a all players, @e all entities and of course set
the player name directly if you wish. I believe any command should be supported, but report any issues here or ob-mc.net discord.


Compiled for 1.21 with Java 21.
