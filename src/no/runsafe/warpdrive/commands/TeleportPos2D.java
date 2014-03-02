package no.runsafe.warpdrive.commands;

import no.runsafe.framework.api.ILocation;
import no.runsafe.framework.api.command.IBranchingExecution;
import no.runsafe.framework.api.command.argument.IArgumentList;
import no.runsafe.framework.api.command.argument.WholeNumber;
import no.runsafe.framework.api.command.player.PlayerCommand;
import no.runsafe.framework.api.player.IPlayer;
import no.runsafe.warpdrive.Engine;

public class TeleportPos2D extends PlayerCommand implements IBranchingExecution
{
	public TeleportPos2D(Engine engine)
	{
		super(
			"teleportpos", "Teleport to the topmost block at the x,z coordinate in your current world.", "runsafe.teleport.coordinate",
			new WholeNumber("x").require(), new WholeNumber("z").require()
		);
		this.engine = engine;
	}

	@Override
	public String OnExecute(IPlayer player, IArgumentList parameters)
	{
		Integer x = parameters.getValue("x");
		Integer z = parameters.getValue("z");
		if (x == null || z == null)
			return "Invalid coordinate";
		ILocation target = player.getLocation();
		target.setX(x);
		target.setZ(z);
		target = engine.findTop(target);
		player.teleport(target);
		return null;
	}

	private final Engine engine;
}