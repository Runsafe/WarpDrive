package no.runsafe.warpdrive.commands;

import no.runsafe.framework.api.IScheduler;
import no.runsafe.framework.api.command.argument.IArgumentList;
import no.runsafe.framework.api.command.player.PlayerAsyncCommand;
import no.runsafe.framework.api.player.IPlayer;
import no.runsafe.warpdrive.database.WarpRepository;

public class DelWarp extends PlayerAsyncCommand
{
	public DelWarp(IScheduler scheduler, WarpRepository repository)
	{
		super(
			"delwarp",
			"Deletes a warp location",
			"runsafe.warp.delete",
			scheduler,
			new WarpArgument(WARP_NAME, repository)
		);
		warpRepository = repository;
	}

	private static final String WARP_NAME = "name";

	@Override
	public String OnAsyncExecute(IPlayer player, IArgumentList parameters)
	{
		String warpName = parameters.getValue(WARP_NAME);
		if (warpRepository.DelPublic(warpName))
			return String.format("&aDeleted public warp %s.", warpName);
		return String.format("&cUnable to delete the public warp %s.", warpName);
	}

	private final WarpRepository warpRepository;
}
