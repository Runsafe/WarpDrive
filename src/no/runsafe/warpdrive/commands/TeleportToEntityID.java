package no.runsafe.warpdrive.commands;

import no.runsafe.framework.command.player.PlayerCommand;
import no.runsafe.framework.server.entity.RunsafeEntity;
import no.runsafe.framework.server.player.RunsafePlayer;
import no.runsafe.warpdrive.Engine;

import java.util.HashMap;

public class TeleportToEntityID extends PlayerCommand
{
	public TeleportToEntityID(Engine engine)
	{
		super("teleporttoentityid", "Teleports you to the given entity in your current world.", "runsafe.teleport.entity", "entityid");
		this.engine = engine;
	}

	@Override
	public String OnExecute(RunsafePlayer player, HashMap<String, String> parameters)
	{
		String entityId = parameters.get("entityid");
		int id = Integer.parseInt(entityId);
		RunsafeEntity entity = player.getWorld().getEntityById(id);
		engine.safePlayerTeleport(entity.getLocation(), player);
		return null;
	}

	private final Engine engine;
}
