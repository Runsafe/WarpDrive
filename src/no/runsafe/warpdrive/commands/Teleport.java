package no.runsafe.warpdrive.commands;

import no.runsafe.framework.command.RunsafePlayerCommand;
import no.runsafe.framework.output.IOutput;
import no.runsafe.framework.server.RunsafeServer;
import no.runsafe.framework.server.player.RunsafePlayer;

import java.util.logging.Level;

import static no.runsafe.warpdrive.StaticWarp.safePlayerTeleport;

public class Teleport extends RunsafePlayerCommand
{
	public Teleport(IOutput output)
	{
		super("teleport", "player");
		console = output;
	}

	@Override
	public boolean CanExecute(RunsafePlayer player, String[] args)
	{
		RunsafePlayer target = null;
		if (args.length > 0)
			target = RunsafeServer.Instance.getPlayer(args[0]);
		if (target == null || !target.isOnline() || !player.canSee(target))
		{
			console.outputDebugToConsole("No teleport target", Level.FINE);
			return true;
		}
		if (player.hasPermission("runsafe.teleport.world.*"))
		{
			console.outputDebugToConsole("Has access to all worlds", Level.FINE);
			return true;
		}
		if (player.hasPermission(String.format("runsafe.teleport.world.%s", target.getWorld().getName())))
		{
			console.outputDebugToConsole("Has access to world " + target.getWorld().getName(), Level.FINE);
			return true;
		}
		return false;
	}

	@Override
	public boolean CouldExecute(RunsafePlayer player)
	{
		return true;
	}

	@Override
	public String OnExecute(RunsafePlayer executor, String[] args)
	{
		String player = getArg("player");
		RunsafePlayer destination = RunsafeServer.Instance.getPlayer(player);
		if (destination == null)
			return String.format("Could not find player %s.", player);

		if(destination.getWorld().getName().equals(executor.getWorld().getName()))
		{
			if(destination.isCreative() && executor.isCreative())
			{
				executor.teleport(destination.getLocation());
				return null;
			}
		}
		if (args.length > 1 && args[1].equals("-f"))
		{
			executor.teleport(destination.getLocation());
			return String.format("Performed unsafe teleport to %s.", player);
		}
		if (safePlayerTeleport(destination.getLocation(), executor, false))
			return null;

		return String.format("Unable to safely teleport to %1$s, try /tp %1$s -f", player);
	}

	final IOutput console;
}
