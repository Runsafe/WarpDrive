package no.runsafe.warpdrive.commands;

import no.runsafe.framework.api.command.ICommandExecutor;
import no.runsafe.framework.api.command.argument.ITabComplete;
import no.runsafe.framework.api.command.argument.IValueExpander;
import no.runsafe.framework.api.command.argument.RequiredArgument;
import no.runsafe.framework.api.player.IPlayer;
import no.runsafe.warpdrive.database.WarpRepository;

import javax.annotation.Nullable;
import java.util.List;

public class WarpArgument extends RequiredArgument implements ITabComplete, IValueExpander
{
	public WarpArgument(WarpRepository repository)
	{
		super("warp");
		this.repository = repository;
	}

	public WarpArgument(String name, WarpRepository repository)
	{
		super(name);
		this.repository = repository;
	}

	@Override
	public List<String> getAlternatives(IPlayer executor, String partial)
	{
		return repository.GetPublicList();
	}

	@Nullable
	@Override
	public String expand(ICommandExecutor context, @Nullable String value)
	{
		if (value == null)
			return null;
		for (String alternative : repository.GetPublicList())
			if (alternative.toUpperCase().startsWith(value.toUpperCase()))
				return alternative;

		return null;
	}

	private final WarpRepository repository;
}
