package no.runsafe.warpdrive.commands;

import no.runsafe.framework.api.ILocation;
import no.runsafe.framework.api.IScheduler;
import no.runsafe.framework.api.command.IBranchingExecution;
import no.runsafe.framework.api.command.ICommandExecutor;
import no.runsafe.framework.api.command.IContextPermissionProvider;
import no.runsafe.framework.api.command.argument.IArgumentList;
import no.runsafe.framework.api.command.argument.Player;
import no.runsafe.framework.api.command.player.PlayerCommand;
import no.runsafe.framework.api.player.IPlayer;
import no.runsafe.framework.timer.TimedCache;
import no.runsafe.warpdrive.Engine;
import no.runsafe.warpdrive.WarpDrive;
import org.bukkit.GameMode;

public class TeleportSelf extends PlayerCommand implements IContextPermissionProvider, IBranchingExecution
{
	public TeleportSelf(Engine engine, IScheduler scheduler)
	{
		super(
			"teleport", "Teleport to another player", null,
			new Player().onlineOnly().require()
		);
		this.engine = engine;
		this.warned = new TimedCache<>(scheduler, 10);
	}

	@Override
	public String getPermission(ICommandExecutor executor, IArgumentList parameters, String[] args)
	{
		if (executor instanceof IPlayer && parameters.has("player"))
		{
			IPlayer target = parameters.getValue("player");
			if (target == null)
			{
				return null;
			}
			return "runsafe.teleport.world." + target.getWorldName();
		}
		return null;
	}

	@Override
	public String OnExecute(IPlayer player, IArgumentList parameters)
	{
		IPlayer to = parameters.getRequired("player");
		IPlayer warning = warned.Cache(player);
		boolean force = warning != null && warning.equals(to);

		WarpDrive.debug.debugFine("target player %s is at %s", to.getName(), to.getLocation());
		// Calculate where the player should be teleported to
		ILocation targetLocation = to.getLocationBehindPlayer(
			2,
			player.getGameMode() != GameMode.CREATIVE
				&& player.getGameMode() != GameMode.SPECTATOR
		);
		if (targetLocation == null)
		{
			return null;
		}
		WarpDrive.debug.debugFine("Teleporting %s to %s", player.getName(), targetLocation);
		// Keep it real simple when both players are in creative mode
		if (to.getWorldName().equals(player.getWorldName()))
		{
			if (to.isCreative() && player.isCreative() || player.getGameMode() == GameMode.SPECTATOR)
			{
				player.teleport(targetLocation);
				return null;
			}
		}
		// Just teleport on second tp attempt
		if (force)
		{
			player.teleport(targetLocation);
			return String.format("&cPerformed unsafe teleport to %s.", to.getPrettyName());
		}
		// Try to safely teleport
		if (engine.safePlayerTeleport(targetLocation, player))
			return null;

		// Send safety warning to player
		warned.Cache(player, to);
		return String.format("&cUnable to safely teleport you to %1$s&c, repeat command to force.", to.getPrettyName());
	}

	private final Engine engine;
	private final TimedCache<IPlayer, IPlayer> warned;
}
