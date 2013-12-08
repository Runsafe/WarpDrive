package no.runsafe.warpdrive.commands;

import no.runsafe.framework.api.IServer;
import no.runsafe.framework.api.command.ExecutableCommand;
import no.runsafe.framework.api.command.ICommandExecutor;
import no.runsafe.framework.api.command.argument.RequiredArgument;
import no.runsafe.framework.api.command.argument.WorldArgument;
import no.runsafe.warpdrive.SmartWarpScanner;

import java.util.Map;

public class SmartWarp extends ExecutableCommand
{
	public SmartWarp(SmartWarpScanner scanner, IServer server)
	{
		super(
			"smartwarp", "Sets up smart warps", "runsafe.warpdrive.smart",
			new WorldArgument(), new RequiredArgument("radius")
		);
		this.scanner = scanner;
		this.server = server;
	}

	@Override
	public String OnExecute(ICommandExecutor iCommandExecutor, Map<String, String> param)
	{
		scanner.Setup(server.getWorld(param.get("world")), param.get("radius"));
		return "&2Smart-warp scan started";
	}

	private final SmartWarpScanner scanner;
	private final IServer server;
}
