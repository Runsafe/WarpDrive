package no.runsafe.warpdrive;

import no.runsafe.framework.command.player.PlayerAsyncCallbackCommand;
import no.runsafe.framework.server.RunsafeLocation;
import no.runsafe.framework.server.player.RunsafePlayer;
import no.runsafe.framework.timer.IScheduler;

public abstract class PlayerTeleportCommand extends PlayerAsyncCallbackCommand<PlayerTeleportCommand.PlayerTeleport>
{
	protected PlayerTeleportCommand(String name, String description, String permission, IScheduler scheduler, String... args)
	{
		super(name, description, permission, scheduler, args);
	}

	@Override
	public void SyncPostExecute(PlayerTeleport playerTeleport)
	{
		if (playerTeleport.location != null)
		{
			if (playerTeleport.force)
				playerTeleport.player.teleport(playerTeleport.location);
			else
				StaticWarp.safePlayerTeleport(playerTeleport.location, playerTeleport.player, false);
		}
		if (playerTeleport.message != null)
			playerTeleport.player.sendColouredMessage(playerTeleport.message);
	}

	public class PlayerTeleport
	{
		public String message = null;
		public RunsafeLocation location = null;
		public RunsafePlayer player = null;
		public boolean force = false;
	}
}
