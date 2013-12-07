package no.runsafe.warpdrive.commands;

import no.runsafe.framework.api.command.argument.RequiredArgument;
import no.runsafe.framework.api.command.player.PlayerCommand;
import no.runsafe.framework.api.entity.IEntity;
import no.runsafe.framework.api.player.IPlayer;

import java.util.Map;

public class TeleportToEntityID extends PlayerCommand
{
	public TeleportToEntityID()
	{
		super(
			"teleporttoentityid", "Teleports you to the given entity in your current world.", "runsafe.teleport.entity",
			new RequiredArgument("entityid")
		);
	}

	@Override
	public String OnExecute(IPlayer player, Map<String, String> parameters)
	{
		String entityId = parameters.get("entityid");
		int id = Integer.parseInt(entityId);
		IEntity entity = player.getWorld().getEntityById(id);
		player.teleport(entity);
		return null;
	}
}
