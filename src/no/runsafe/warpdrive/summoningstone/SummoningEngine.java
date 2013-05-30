package no.runsafe.warpdrive.summoningstone;

import no.runsafe.framework.event.player.IPlayerRightClickBlock;
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
			if (SummoningStone.isSummoningStone(runsafeBlock.getLocation()))
			{
				runsafePlayer.sendColouredMessage("You just set up a portal.");
				return false;
			}
		}
		return true;
	}
}
