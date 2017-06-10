package no.runsafe.warpdrive.commands;

import no.runsafe.framework.api.command.ICommandExecutor;
import no.runsafe.framework.api.command.argument.ITabComplete;
import no.runsafe.framework.api.command.argument.IValueExpander;
import no.runsafe.framework.api.command.argument.OptionalArgument;
import no.runsafe.framework.api.player.IPlayer;
import no.runsafe.warpdrive.database.WarpRepository;

import javax.annotation.Nullable;
import java.util.List;

public class HomeArgument extends OptionalArgument implements ITabComplete, IValueExpander
{
	public HomeArgument(WarpRepository warpRepository)
	{
		super("home");
		this.warpRepository = warpRepository;
	}

	public HomeArgument(String name, WarpRepository warpRepository)
	{
		super(name);
		this.warpRepository = warpRepository;
	}

	@Override
	public List<String> getAlternatives(IPlayer player, String arg)
	{
		return warpRepository.GetPrivateList(player);
	}

	@Nullable
	@Override
	public String expand(ICommandExecutor context, @Nullable String value)
	{
		if (value == null)
			return null;
		for (String alternative : warpRepository.GetPrivateList((IPlayer) context))
			if (alternative.toUpperCase().startsWith(value.toUpperCase()))
				return alternative;

		return null;
	}

	private final WarpRepository warpRepository;
}
