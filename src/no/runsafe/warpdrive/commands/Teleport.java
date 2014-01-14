package no.runsafe.warpdrive.commands;

import no.runsafe.framework.api.IScheduler;
import no.runsafe.framework.api.command.ICommandExecutor;
import no.runsafe.framework.api.command.IContextPermissionProvider;
import no.runsafe.framework.api.command.argument.IArgumentList;
import no.runsafe.framework.api.command.argument.OnlinePlayerArgument;
import no.runsafe.framework.api.command.player.PlayerCommand;
import no.runsafe.framework.api.player.IAmbiguousPlayer;
import no.runsafe.framework.api.player.IPlayer;
import no.runsafe.framework.timer.TimedCache;
import no.runsafe.warpdrive.Engine;

public class Teleport extends PlayerCommand implements IContextPermissionProvider
{
	public Teleport(Engine engine, IScheduler scheduler)
	{
		super(
			"teleport", "Teleports you or another player to another player", null,
			new OnlinePlayerArgument("player1", true), new OnlinePlayerArgument("player2", false)
		);
		this.engine = engine;
		this.warned = new TimedCache<String, String>(scheduler, 10);
	}

	@Override
	public String getPermission(ICommandExecutor executor, IArgumentList parameters, String[] args)
	{
		if (executor instanceof IPlayer)
		{
			IPlayer target;
			if (!parameters.containsKey("player2"))
				target = parameters.getPlayer("player1");
			else
				target = parameters.getPlayer("player2");
			if (target == null)
				return null;

			return "runsafe.teleport.world." + target.getWorldName();
		}
		return null;
	}

	@Override
	public String OnExecute(IPlayer player, IArgumentList parameters)
	{
		String movePlayer;
		String toPlayer;
		IPlayer move;
		IPlayer to;
		if (parameters.containsKey("player2"))
		{
			movePlayer = parameters.get("player1");
			move = parameters.getPlayer("player1");
			toPlayer = parameters.get("player2");
			to = parameters.getPlayer("player2");
		}
		else
		{
			movePlayer = player.getName();
			move = player;
			toPlayer = parameters.get("player1");
			to = parameters.getPlayer("player1");
		}

		if (move instanceof IAmbiguousPlayer)
			return move.toString();

		if (to instanceof IAmbiguousPlayer)
			return to.toString();

		if (move == null || !move.isOnline() || player.shouldNotSee(move))
			return String.format("Could not find player %s to teleport", movePlayer);

		if (to == null || !to.isOnline() || player.shouldNotSee(to))
			return String.format("Could not find destination player %s.", toPlayer);

		String warning = warned.Cache(player.getName());
		boolean force = warning != null && warning.equals(toPlayer);

		if (to.getWorldName().equals(move.getWorldName()))
		{
			if (to.isCreative() && move.isCreative())
			{
				move.teleport(to.getLocation());
				return null;
			}
		}
		if (force)
		{
			move.teleport(to.getLocation());
			return String.format("Performed unsafe teleport of %s to %s.", move.getPrettyName(), to.getPrettyName());
		}
		if (engine.safePlayerTeleport(to.getLocation(), move))
			return null;

		warned.Cache(player.getName(), toPlayer);
		return String.format("Unable to safely teleport %1$s to %2$s, repeat command to force.", move.getPrettyName(), to.getPrettyName());
	}

	private final Engine engine;
	private final TimedCache<String, String> warned;
}
