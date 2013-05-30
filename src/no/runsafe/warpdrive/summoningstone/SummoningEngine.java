package no.runsafe.warpdrive.summoningstone;

import no.runsafe.framework.configuration.IConfiguration;
import no.runsafe.framework.event.IConfigurationChanged;
import no.runsafe.framework.event.entity.IEntityPortalEnterEvent;
import no.runsafe.framework.event.player.IPlayerRightClickBlock;
import no.runsafe.framework.server.RunsafeLocation;
import no.runsafe.framework.server.RunsafeServer;
import no.runsafe.framework.server.block.RunsafeBlock;
import no.runsafe.framework.server.entity.PassiveEntity;
import no.runsafe.framework.server.entity.RunsafeEntity;
import no.runsafe.framework.server.entity.RunsafeItem;
import no.runsafe.framework.server.event.entity.RunsafeEntityPortalEnterEvent;
import no.runsafe.framework.server.item.RunsafeItemStack;
import no.runsafe.framework.server.item.meta.RunsafeItemMeta;
import no.runsafe.framework.server.player.RunsafePlayer;
import no.runsafe.framework.timer.IScheduler;
import org.bukkit.Material;

import java.util.HashMap;
import java.util.List;

public class SummoningEngine implements IPlayerRightClickBlock, IConfigurationChanged, IEntityPortalEnterEvent
{
	public SummoningEngine(SummoningStoneRepository summoningStoneRepository, IScheduler scheduler)
	{
		this.summoningStoneRepository = summoningStoneRepository;
		this.scheduler = scheduler;
	}

	@Override
	public boolean OnPlayerRightClick(RunsafePlayer runsafePlayer, RunsafeItemStack itemStack, RunsafeBlock runsafeBlock)
	{
		// Player has set fire to emerald block?
		if (itemStack.getType() == Material.FLINT_AND_STEEL && runsafeBlock.getTypeId() == Material.EMERALD_BLOCK.getId())
		{
			RunsafeLocation stoneLocation = runsafeBlock.getLocation();
			if (SummoningStone.isSummoningStone(stoneLocation))
			{
				final int stoneID = this.summoningStoneRepository.addSummoningStone(stoneLocation);
				SummoningStone summoningStone = new SummoningStone(stoneLocation);
				summoningStone.activate();

				summoningStone.setTimerID(this.scheduler.startSyncTask(new Runnable() {
					@Override
					public void run() {
						summoningStoneExpire(stoneID);
					}
				}, this.stoneExpireTime));

				this.stones.put(stoneID, summoningStone);
				return false;
			}
		}
		return true;
	}

	@Override
	public void OnEntityPortalEnter(RunsafeEntityPortalEnterEvent event)
	{
		RunsafeEntity entity = event.getEntity();
		if (entity.getEntityType() == PassiveEntity.DroppedItem)
		{
			RunsafeItemStack item = ((RunsafeItem) entity).getItemStack();
			if (item.getItemId() == Material.WRITTEN_BOOK.getId())
			{
				RunsafeItemMeta meta = item.getItemMeta();

				if (meta != null)
				{
					List<String> lore = meta.getLore();
					if (!lore.isEmpty())
						RunsafeServer.Instance.broadcastMessage(lore.get(0));
				}
			}
		}
	}

	public void summoningStoneExpire(int stoneID)
	{
		// If this is false, we actually had a fault somewhere, but we check anyway.
		if (this.stones.containsKey(stoneID))
		{
			// The stone expired, remove it from everything.
			SummoningStone stone = this.stones.get(stoneID);
			stone.remove();
			this.summoningStoneRepository.deleteSummoningStone(stoneID);
			this.stones.remove(stoneID);
		}
	}

	public HashMap<Integer, SummoningStone> getLoadedStones()
	{
		return this.stones;
	}

	@Override
	public void OnConfigurationChanged(IConfiguration config)
	{
		this.stoneExpireTime = config.getConfigValueAsInt("summoningStone.expire") * 3600;
	}

	private int stoneExpireTime = 36000;
	private HashMap<Integer, SummoningStone> stones = new HashMap<Integer, SummoningStone>();
	private SummoningStoneRepository summoningStoneRepository;
	private IScheduler scheduler;
}
