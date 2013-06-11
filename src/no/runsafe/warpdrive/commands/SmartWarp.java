package no.runsafe.warpdrive.commands;

import no.runsafe.framework.api.command.ExecutableCommand;
import no.runsafe.framework.api.command.ICommandExecutor;
import no.runsafe.framework.minecraft.RunsafeServer;
import no.runsafe.warpdrive.SmartWarpScanner;

import java.util.HashMap;

public class SmartWarp extends ExecutableCommand
{
	public SmartWarp(SmartWarpScanner scanner)
	{
		super("smartwarp", "Sets up smart warps", "runsafe.warpdrive.smart", "world", "radius");
		this.scanner = scanner;
	}

	@Override
	public String OnExecute(ICommandExecutor iCommandExecutor, HashMap<String, String> param)
	{
		scanner.Setup(RunsafeServer.Instance.getWorld(param.get("world")), param.get("radius"));
		return "&2Smart-warp scan started";
	}

	private SmartWarpScanner scanner;
}
