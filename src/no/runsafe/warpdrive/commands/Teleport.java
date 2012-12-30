package no.runsafe.warpdrive.commands;

import no.runsafe.framework.command.RunsafePlayerCommand;
import no.runsafe.framework.output.IOutput;
import no.runsafe.framework.server.RunsafeServer;
import no.runsafe.framework.server.player.RunsafeAmbiguousPlayer;
import no.runsafe.framework.server.player.RunsafePlayer;
import org.apache.commons.lang.StringUtils;

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
			target = RunsafeServer.Instance.getOnlinePlayer(player, args[0]);
		if (target == null || !target.isOnline() || !player.canSee(target) || target instanceof RunsafeAmbiguousPlayer)
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
		String movePlayer;
		RunsafePlayer move;
		String toPlayer;
		RunsafePlayer to;
		if (args.length > 1)
		{
			movePlayer = getArg("player");
			move = RunsafeServer.Instance.getOnlinePlayer(executor, movePlayer);
			toPlayer = args[1];
			to = RunsafeServer.Instance.getOnlinePlayer(executor, toPlayer);
		}
		else
		{
			movePlayer = executor.getName();
			move = executor;
			toPlayer = getArg("player");
			to = RunsafeServer.Instance.getOnlinePlayer(executor, toPlayer);
		}

		if (move instanceof RunsafeAmbiguousPlayer)
			return formatAmbiguity((RunsafeAmbiguousPlayer) move);

		if (to instanceof RunsafeAmbiguousPlayer)
			return formatAmbiguity((RunsafeAmbiguousPlayer) to);

		if (move == null || !move.isOnline())
			return String.format("Could not find player %s to teleport.", movePlayer);

		if (to == null || !to.isOnline())
			return String.format("Could not find destination player %s.", toPlayer);

		if (to.getWorld().getName().equals(move.getWorld().getName()))
		{
			if (to.isCreative() && move.isCreative())
			{
				move.teleport(to.getLocation());
				return null;
			}
		}
		if (args.length > 1 && args[1].equals("-f"))
		{
			move.teleport(to.getLocation());
			return String.format("Performed unsafe teleport of %s to %s.", movePlayer, toPlayer);
		}
		if (safePlayerTeleport(to.getLocation(), move, false))
			return null;

		return String.format("Unable to safely teleport %1$s to %2$s, try /tp %1$s %2$s -f", movePlayer, toPlayer);
	}

	private String formatAmbiguity(RunsafeAmbiguousPlayer player)
	{
		return String.format(
			"Multiple players found, please specify better: %s",
			StringUtils.join(player.getAmbiguity(), ", ")
		);
	}

	final IOutput console;
}
