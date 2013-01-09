package no.runsafe.warpdrive.commands;

import no.runsafe.framework.command.RunsafePlayerCommand;
import no.runsafe.framework.server.entity.RunsafeEntity;
import no.runsafe.framework.server.player.RunsafePlayer;
import no.runsafe.warpdrive.StaticWarp;

public class TeleportToEntityID extends RunsafePlayerCommand
{
	public TeleportToEntityID()
	{
		super("teleporttoentityid", "entityid");
	}

	@Override
	public String requiredPermission()
	{
		return "runsafe.teleport.entity";
	}

	@Override
	public String OnExecute(RunsafePlayer executor, String[] args)
	{
		String entityId = getArg("entityid");
		int id = Integer.parseInt(entityId);
		RunsafeEntity entity = executor.getWorld().getEntityById(id);
		StaticWarp.safePlayerTeleport(entity.getLocation(), executor, false);
		return null;
	}
}
