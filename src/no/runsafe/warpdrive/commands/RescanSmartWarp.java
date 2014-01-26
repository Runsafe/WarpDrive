package no.runsafe.warpdrive.commands;

import no.runsafe.framework.api.command.ExecutableCommand;
import no.runsafe.framework.api.command.ICommandExecutor;
import no.runsafe.framework.api.command.argument.IArgumentList;
import no.runsafe.framework.api.command.argument.RequiredArgument;
import no.runsafe.framework.api.command.argument.WorldArgument;
import no.runsafe.warpdrive.SmartWarpScanner;

public class RescanSmartWarp extends ExecutableCommand
{
	public RescanSmartWarp(SmartWarpScanner scanner)
	{
		super("rescansmartwarp", "Clears out cached data for a smartwarp and reinitializes it.", "runsafe.warpdrive.smart",
			new WorldArgument(), new RequiredArgument("radius")
		);
		this.scanner = scanner;
	}

	@Override
	public String OnExecute(ICommandExecutor executor, IArgumentList param)
	{
		scanner.Setup(param.getWorld("world"), param.get("radius"), true);
		return "&2Smart-warp scan re-started";
	}

	private final SmartWarpScanner scanner;
}
