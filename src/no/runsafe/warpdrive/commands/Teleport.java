package no.runsafe.warpdrive.commands;

import no.runsafe.framework.api.IScheduler;
import no.runsafe.framework.api.IServer;
import no.runsafe.framework.api.command.ICommandExecutor;
import no.runsafe.framework.api.command.IContextPermissionProvider;
import no.runsafe.framework.api.command.argument.PlayerArgument;
import no.runsafe.framework.api.command.player.PlayerCommand;
import no.runsafe.framework.api.player.IAmbiguousPlayer;
import no.runsafe.framework.api.player.IPlayer;
import no.runsafe.framework.timer.TimedCache;
import no.runsafe.warpdrive.Engine;

import java.util.Map;

public class Teleport extends PlayerCommand implements IContextPermissionProvider
{
	public Teleport(Engine engine, IScheduler scheduler, IServer server)
	{
		super(
			"teleport", "Teleports you or another player to another player", null,
			new PlayerArgument("player1", true), new PlayerArgument("player2", false)
		);
		this.engine = engine;
		this.server = server;
		this.warned = new TimedCache<String, String>(scheduler, 10);
	}

	@Override
	public String getPermission(ICommandExecutor executor, Map<String, String> parameters, String[] args)
	{
		if (executor instanceof IPlayer)
		{
			IPlayer teleporter = (IPlayer) executor;
			IPlayer target;
			if (!parameters.containsKey("player2"))
				target = server.getOnlinePlayer(teleporter, parameters.get("player1"));
			else
				target = server.getOnlinePlayer(teleporter, parameters.get("player2"));
			if (target == null)
				return null;

			return "runsafe.teleport.world." + target.getWorld().getName();
		}
		return null;
	}

	@Override
	public String OnExecute(IPlayer player, Map<String, String> parameters)
	{
		String movePlayer;
		IPlayer move;
		String toPlayer;
		IPlayer to;
		if (parameters.containsKey("player2"))
		{
			movePlayer = parameters.get("player1");
			move = server.getOnlinePlayer(player, movePlayer);
			toPlayer = parameters.get("player2");
			to = server.getOnlinePlayer(player, toPlayer);
		}
		else
		{
			movePlayer = player.getName();
			move = player;
			toPlayer = parameters.get("player1");
			to = server.getOnlinePlayer(player, toPlayer);
		}

		if (move instanceof IAmbiguousPlayer)
			return move.toString();

		if (to instanceof IAmbiguousPlayer)
			return to.toString();

		if (move == null || !move.isOnline() || player.shouldNotSee(move))
			return String.format("Could not find player %s to teleport.", movePlayer);

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
	private final IServer server;
}
