# OBJoinCommands
Minecraft bukkit/spigot plugin to set a command to be run when a player joins a world or the server.

Useful for your lobby or game servers where you want something specific done when players join.

Use the /objc command to add or remove a command for the server or a specific world.

Commands:
/objc list [ server | <world> ] - to show current commands
/objc add [ server | world <worldname> ] <sequence> "<command>" - to set a command at a specific sequence position for a world or the server
/objc del [ server | world <worldname> ] <sequence> - to remove a server or world command at a specific position

Examples:

/objc add server 1 give @p diamond_sword 1 - will give every player a diamond sword in joining the server

Note that the sequence number will be 1 for the first command for that world or the server. Subsequent commands will be the next logical 
sequence number regardless of the sequence number you give, since you can't have gaps.

/objc add world world_nether 1 give @p cooked_beef 10 - will give the player 10 steaks when joining or teleporting to the nether.

/objc list - will list all commands for the server and any worlds that have them registered.
/objc list world world_the_end - will list the commands for just The End world.

/objc del server 5 - will remove command at sequence number 5 if the command exists. Note that all commands above will move down in the
sequence order, so if there was a command with sequence number 6 then it will be sequence 5 following that command, and so on.

objc del world world 2 - will remove the command at sequence number 2 for the overworld.

Currently any command that fails will fail silently. Something to be worked on later so we get better error reporting. I didn't want to
spam the chat with fail messages if commands are broken.


Compiled for 1.18 with Java 17, but should work with older versions (up to a point).
