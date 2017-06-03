package no.runsafe.warpdrive.commands;

import no.runsafe.framework.api.ILocation;
import no.runsafe.framework.api.IScheduler;
import no.runsafe.framework.api.block.ISign;
import no.runsafe.framework.api.command.ICommandExecutor;
import no.runsafe.framework.api.command.argument.IArgumentList;
import no.runsafe.framework.api.command.argument.RequiredArgument;
import no.runsafe.framework.api.event.player.IPlayerRightClickSign;
import no.runsafe.framework.api.log.IConsole;
import no.runsafe.framework.api.player.IPlayer;
import no.runsafe.framework.minecraft.item.meta.RunsafeMeta;
import no.runsafe.framework.text.ChatColour;
import no.runsafe.warpdrive.Engine;
import no.runsafe.warpdrive.database.WarpRepository;
import org.apache.commons.lang.StringUtils;

import javax.annotation.Nonnull;
import java.util.ArrayList;

public class Warp extends PlayerTeleportCommand implements IPlayerRightClickSign
{
	public Warp(WarpRepository repository, IConsole output, IScheduler scheduler, Engine engine)
	{
		super(
			"warp", "Teleports you to a predefined warp location", "runsafe.warp.use.<destination>", scheduler, engine,
			new RequiredArgument("destination")
		);
		warpRepository = repository;
		console = output;
	}

	@Override
	public PlayerTeleport OnAsyncExecute(IPlayer player, IArgumentList parameters)
	{
		PlayerTeleport target = new PlayerTeleport();
		target.force = true;
		target.player = player;
		target.location = warpRepository.GetPublic(((String) parameters.getValue("destination")).toLowerCase());
		if (target.location == null)
			target.message = String.format("The warp %s does not exist.", (String) parameters.getValue("destination"));
		return target;
	}

	@Nonnull
	@Override
	public String getUsage(@Nonnull ICommandExecutor executor)
	{
		ArrayList<String> warps = new ArrayList<String>();
		for (String warp : warpRepository.GetPublicList())
			if (executor.hasPermission(String.format("runsafe.warp.use.%s", warp)))
				warps.add(warp);
		if (warps.isEmpty())
			return "\nNo warps available to you.";
		return String.format("\nExisting warps: %1$s", StringUtils.join(warps, ", "));
	}

	@Override
	public boolean OnPlayerRightClickSign(IPlayer player, RunsafeMeta itemStack, ISign sign)
	{
		if (!sign.getLine(0).contains(signHeader))
			return true;

		String name = sign.getLine(1).toLowerCase();
		ILocation destination = warpRepository.GetPublic(name);
		if (destination == null)
		{
			console.logWarning("%s used a invalid warp sign %s.", player.getName(), name);
			return false;
		}
		if (!player.hasPermission("runsafe.warpsign.use.*")
			&& !player.hasPermission(String.format("runsafe.warpsign.use.%s", name)))
			return false;

		player.teleport(destination);
		return false;
	}

	private final WarpRepository warpRepository;
	private final IConsole console;
	public static final String signHeader = ChatColour.DARK_BLUE.toBukkit() + "[warp]";
	public static final String signTag = "[warp]";
}
