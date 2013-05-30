package no.runsafe.warpdrive.summoningstone;

import no.runsafe.framework.event.player.IPlayerRightClickBlock;
import no.runsafe.framework.server.RunsafeLocation;
import no.runsafe.framework.server.block.RunsafeBlock;
import no.runsafe.framework.server.item.RunsafeItemStack;
import no.runsafe.framework.server.player.RunsafePlayer;
import org.bukkit.Material;

public class SummoningEngine implements IPlayerRightClickBlock
{
	@Override
	public boolean OnPlayerRightClick(RunsafePlayer runsafePlayer, RunsafeItemStack itemStack, RunsafeBlock runsafeBlock)
	{
		// Player has set fire to emerald block?
		if (itemStack.getType() == Material.FLINT_AND_STEEL && runsafeBlock.getTypeId() == Material.EMERALD_BLOCK.getId())
		{
			RunsafeLocation stoneLocation = runsafeBlock.getLocation();
			if (SummoningStone.isSummoningStone(stoneLocation))
			{
				SummoningStone summoningStone = new SummoningStone(stoneLocation);
				summoningStone.activate();
				return false;
			}
		}
		return true;
	}
}
