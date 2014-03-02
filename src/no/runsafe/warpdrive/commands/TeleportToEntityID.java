package no.runsafe.warpdrive.commands;

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

	@SuppressWarnings("ConstantConditions")
	@Override
	public String OnExecute(IPlayer player, IArgumentList parameters)
	{
		Integer entityId = parameters.getValue("entityid");
		IEntity entity = player.getWorld().getEntityById(entityId);
		player.teleport(entity);
		return null;
	}
}
