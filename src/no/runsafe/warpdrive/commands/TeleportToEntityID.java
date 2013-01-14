package no.runsafe.warpdrive.commands;

import no.runsafe.framework.command.player.PlayerCommand;
import no.runsafe.framework.server.entity.RunsafeEntity;
import no.runsafe.framework.server.player.RunsafePlayer;
import no.runsafe.warpdrive.StaticWarp;

import java.util.HashMap;

public class TeleportToEntityID extends PlayerCommand
{
	public TeleportToEntityID()
	{
		super("teleporttoentityid", "Teleports you to the given entity in your current world.", "runsafe.teleport.entity", "entityid");
	}

	@Override
	public String OnExecute(RunsafePlayer player, HashMap<String, String> parameters, String[] args)
	{
		String entityId = parameters.get("entityid");
		int id = Integer.parseInt(entityId);
		RunsafeEntity entity = player.getWorld().getEntityById(id);
		StaticWarp.safePlayerTeleport(entity.getLocation(), player, false);
		return null;
	}
}
