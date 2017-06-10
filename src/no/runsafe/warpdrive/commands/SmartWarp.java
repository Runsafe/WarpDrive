package no.runsafe.warpdrive.commands;

import no.runsafe.framework.api.command.ExecutableCommand;
import no.runsafe.framework.api.command.ICommandExecutor;
import no.runsafe.framework.api.command.argument.IArgumentList;
import no.runsafe.framework.api.command.argument.WholeNumber;
import no.runsafe.framework.api.command.argument.WorldArgument;
import no.runsafe.warpdrive.smartwarp.SmartWarpScanner;

public class SmartWarp extends ExecutableCommand
{
	public SmartWarp(SmartWarpScanner scanner)
	{
		super(
			"smartwarp", "Sets up smart warps", "runsafe.warpdrive.smart",
			new WorldArgument().require(), new WholeNumber("radius").require()
		);
		this.scanner = scanner;
	}

	@Override
	public String OnExecute(ICommandExecutor iCommandExecutor, IArgumentList param)
	{
		scanner.Setup(param.getValue("world"), param.getValue("radius"), false);
		return "&2Smart-warp scan started";
	}

	private final SmartWarpScanner scanner;
}
