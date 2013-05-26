package no.runsafe.warpdrive.commands;

import no.runsafe.framework.command.player.PlayerCommand;
import no.runsafe.framework.server.RunsafeServer;
import no.runsafe.framework.server.player.RunsafePlayer;
import no.runsafe.warpdrive.SmartWarpScanner;

import java.util.HashMap;

public class SmartWarp extends PlayerCommand
{
	public SmartWarp(SmartWarpScanner scanner)
	{
		super("smartwarp", "Sets up smart warps", "runsafe.warpdrive.smart", "world", "radius");
		this.scanner = scanner;
	}

	@Override
	public String OnExecute(RunsafePlayer player, HashMap<String, String> param)
	{
		scanner.Setup(RunsafeServer.Instance.getWorld(param.get("world")), param.get("radius"));
		return null;
	}

	private SmartWarpScanner scanner;
}
