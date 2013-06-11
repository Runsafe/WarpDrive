package no.runsafe.warpdrive.commands;

import no.runsafe.framework.api.command.player.PlayerCommand;
import no.runsafe.framework.minecraft.entity.RunsafeEntity;
import no.runsafe.framework.minecraft.player.RunsafePlayer;

import java.util.HashMap;

public class TeleportToEntityID extends PlayerCommand
{
	public TeleportToEntityID()
	{
		super("teleporttoentityid", "Teleports you to the given entity in your current world.", "runsafe.teleport.entity", "entityid");
	}

	@Override
	public String OnExecute(RunsafePlayer player, HashMap<String, String> parameters)
	{
		String entityId = parameters.get("entityid");
		int id = Integer.parseInt(entityId);
		RunsafeEntity entity = player.getWorld().getEntityById(id);
		player.teleport(entity.getLocation());
		return null;
	}
}
