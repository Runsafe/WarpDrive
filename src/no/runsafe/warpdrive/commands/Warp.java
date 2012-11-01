package no.runsafe.warpdrive.commands;

import no.runsafe.framework.command.RunsafeAsyncPlayerCommand;
import no.runsafe.framework.event.block.ISignChange;
import no.runsafe.framework.event.player.IPlayerRightClickSign;
import no.runsafe.framework.output.IOutput;
import no.runsafe.framework.server.RunsafeLocation;
import no.runsafe.framework.server.block.RunsafeBlock;
import no.runsafe.framework.server.block.RunsafeSign;
import no.runsafe.framework.server.item.RunsafeItemStack;
import no.runsafe.framework.server.player.RunsafePlayer;
import no.runsafe.framework.timer.IScheduler;
import no.runsafe.warpdrive.StaticWarp;
import no.runsafe.warpdrive.database.WarpRepository;
import org.bukkit.ChatColor;

public class Warp extends RunsafeAsyncPlayerCommand implements IPlayerRightClickSign, ISignChange
{
	public Warp(IScheduler scheduler, WarpRepository repository, IOutput output)
	{
		super("warp", scheduler, "destination");
		warpRepository = repository;
		console = output;
	}

	@Override
	public String requiredPermission()
	{
		return "runsafe.warp.use";
	}

	@Override
	public String OnExecute(RunsafePlayer player, String[] strings)
	{
		RunsafeLocation destination = warpRepository.GetPublic(getArg("destination"));
		StaticWarp.safePlayerTeleport(destination, player, false);
		return "";
	}

	@Override
	public boolean OnPlayerRightClickSign(RunsafePlayer player, RunsafeItemStack itemStack, RunsafeSign sign)
	{
		if (!sign.getLine(0).contains("[warp]"))
			return true;

		String name = sign.getLine(1).toLowerCase();
		RunsafeLocation destination = warpRepository.GetPublic(name);
		if (destination == null)
		{
			console.write(String.format("%s used a invalid warp sign %s.", player.getName(), name));
			return false;
		}
		if (player.hasPermission("runsafe.warpsign.use.*")
			|| player.hasPermission(String.format("runsafe.warpsign.use.%s", name)))
			StaticWarp.safePlayerTeleport(destination, player, false);

		return false;
	}

	@Override
	public boolean OnSignChange(RunsafePlayer player, RunsafeBlock runsafeBlock, String[] strings)
	{
		if (!strings[0].toLowerCase().contains("[warp]"))
			return true;
		if(player.hasPermission("runsafe.warpsign.create"))
		{
			((RunsafeSign)runsafeBlock.getBlockState()).setLine(0, ChatColor.BLUE + "[warp]");
			return true;
		}
		return false;
	}

	WarpRepository warpRepository;
	IOutput console;
}
