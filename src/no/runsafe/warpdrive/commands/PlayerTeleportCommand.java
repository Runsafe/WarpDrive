package no.runsafe.warpdrive.commands;

import no.runsafe.framework.api.ILocation;
import no.runsafe.framework.api.IScheduler;
import no.runsafe.framework.api.command.argument.IArgument;
import no.runsafe.framework.api.command.player.PlayerAsyncCallbackCommand;
import no.runsafe.framework.api.player.IPlayer;
import no.runsafe.warpdrive.Engine;

public abstract class PlayerTeleportCommand extends PlayerAsyncCallbackCommand<PlayerTeleportCommand.PlayerTeleport>
{
	protected PlayerTeleportCommand(String name, String description, String permission, IScheduler scheduler, Engine engine, IArgument... args)
	{
		super(name, description, permission, scheduler, args);
		this.engine = engine;
	}

	@Override
	public void SyncPostExecute(PlayerTeleport playerTeleport)
	{
		if (playerTeleport == null)
		{
			return;
		}
		if (playerTeleport.location != null)
		{
			if (playerTeleport.force)
				playerTeleport.player.teleport(playerTeleport.location);
			else
				engine.safePlayerTeleport(playerTeleport.location, playerTeleport.player);
		}
		if (playerTeleport.message != null)
			playerTeleport.player.sendColouredMessage(playerTeleport.message);
	}

	private final Engine engine;

	public static class PlayerTeleport
	{
		public String message = null;
		public ILocation location = null;
		public IPlayer player = null;
		public boolean force = false;
	}
}
