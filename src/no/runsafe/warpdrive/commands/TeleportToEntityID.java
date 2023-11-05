package no.runsafe.warpdrive.commands;

import no.runsafe.framework.api.IWorld;
import no.runsafe.framework.api.command.argument.IArgumentList;
import no.runsafe.framework.api.command.argument.WholeNumber;
import no.runsafe.framework.api.command.player.PlayerCommand;
import no.runsafe.framework.api.entity.IEntity;
import no.runsafe.framework.api.player.IPlayer;

public class TeleportToEntityID extends PlayerCommand
{
	public TeleportToEntityID()
	{
		super(
			"teleporttoentityid", "Teleports you to the given entity in your current world.", "runsafe.teleport.entity",
			new WholeNumber("entityid").require()
		);
	}

	@Override
	public String OnExecute(IPlayer player, IArgumentList parameters)
	{
		int entityId = parameters.getRequired("entityid");
		IWorld world = player.getWorld();
		if (world == null)
		{
			return null;
		}
		IEntity entity = world.getEntityById(entityId);
		player.teleport(entity);
		return null;
	}
}
