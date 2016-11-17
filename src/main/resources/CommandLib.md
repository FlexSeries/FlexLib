# FlexLib Command Framework

## Features

- Annotation-based command system
- Powerful and flexible argument resolving
    - Commands are treated as methods
    
## Writing a Command

- A command handler method must be annotated with CommandHandler
- The function must accept a command sender as the first argument and a command context as the second, then the arguments after it
- Function can return a string or message to be sent to the player, or throw an exception if it fails during execution.
- If the function accepts a Player as the first argument, then it is implicitly player-only.
- Parameter Annotations:
    - `@Default('raw argument')`: Sets the default value for the parameter.
    
## IDEAS (temp)
 
- Command handler annotation can verify if a specified module is enabled
- Reverse arguments `/cmd [player] info`
    - `player [] info` -> Indicates that the subcommand is a reverse subcommand since it has argument(s) prior to it.
- Commands that schedule background task and prevent re-execution while running in the background.
- Not every command will require command context as second argument
    
## Command Execution Process

1) Command begins execution at the base registered command.
2) Subcommands are traversed until the last one is found.

## Default Argument Parsers

### `org.bukkit.entity.Player`

An online player.

Default: {SENDER} -> the sender of the command

#### Options:
- notSender: true/false
