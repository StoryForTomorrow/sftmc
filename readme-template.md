sftmc [example]
===========
Folder: <i>src</i><p>
Package: <i>org.teammoose.command</i><br>
Since: <i>0.0.1-SNAPSHOT</i><p>
<b>Description:</b><br>
<i>this package contains the command framework and the command classes that use the framework</i><p>

Classes
---

- <b><i>framework.CommandFramework</i></b><br>
<i><b>Added TM-0.0.1-SNAPSHOT</b>
	
	<b>Written originally by minnymin3 modified by JohnnPM</b><i>
	
	The framework for handling the commands. 
	Annotation system for commands.
	Dynamically looks through the classes and registers all the
	command classes that implement the CommandListener interface.
	
	Any commands registered with this framework do not need to be registered 
	inside of the plugin.yml
	
	To implement this into the plugin you would have to Override
	the onCommand method inside your main class and have it return
	the <code>handleCommand(...)</code> method inside of the framework
	class:
	<pre>
	public class AwesomeMooseExamplePlugin extends JavaPlugin {

    CommandFramework framework;

    public void onEnable() {
        framework = new CommandFramework(this);
        // This will register all the @Command annotations inside of all the classes implement the CommandListener class
        // Note: Commands do not need to be registered in plugin.yml
        framework.registerCommands(this);
        // Call this method if you want to register the commands with the 
        framework.registerHelp(this);
    }

    @Override
    public void onCommand(CommandSender sender, Command cmd, String label, String[] args)
    {
    	return framework.handleCommand(sender, label, cmd, args);
    }
	</pre><p>
	
	Example Command Class:
	<pre>
	public class AwesomeMooseCommand implements CommandListener
	{
		// This is a basic command
		@Command(name = "tm", aliases = { "teammoose"})
		public void testSub(CommandArgs info) {
    		info.getSender().sendMessage("mooses r awesome");
		}
		
		// You can have multiple command annotations inside of one class, all of them being a different command
		// Also doing "tm.subcommand" is just as it says, it creates a subcommand under the other command defined.
		@Command(name = "tm.subcommand", aliases = { "teammoose.subcommand"})
		public void testSub(CommandArgs info) {
    		info.getSender().sendMessage("mooses r moar awesome then u");
		}
	}
	</pre>
	
	Any class implementing <code>CommandListener</code> will be searched for the @Command
	annotation and those command annotations will automatically be registered with the framework.
	
	@Command Annotation Fields:
	
	| Name | Description | Required | Default |
	|------|-------------|----------|---------|
	| command() | The command's name to be registered under. | YES | N/A |
	| permission() | The permission required to be able to use the command | NO | NONE |
	| noPerm() | The no permission message to be displayed if the sender does not have permission to perform the command | NO | "Much Deny. So Wow." |
	| aliases() | The list of aliases that the command can also be called by. | NO | NONE | 
	| description() | The description of the command that will be displayed in the /help list if registered | NO | NONE |
	| usage() |  The usage message that will be displayed if the command is not handled | NO | NONE |

Commands
---

- <b><i>TagCommand</i></b><br>
<i><b>Added TM-0.0.1-SNAPSHOT</b>
	
	Command: /tag
	Arguments: <br>
	/tag set <prefix|suffix> <player> <prefix/suffix...> : Adds the defined prefix/suffix
	/tag remove <prefix|suffix|all> <player> : removes either the prefix, suffix, or both from the defined player.
			
	This command modifies the player's tag using the TagUtil
	util file. This creates a player's team if they don't already
	have one, adds them to their team, and then adds the desired
	prefix/suffix.
	
	When the remove command is run it sets the prefix/suffix to ""
	and if all the tags are removed the player is removed from
	his/her team and the team is unregisted on the scoreboard to
	prevent bugs with the system.
	
	TODO: Fix the playerHasTags(Player) method inside of TagUtil
</i>

---
TeamMoose 2014 • http://www.teammoose.org