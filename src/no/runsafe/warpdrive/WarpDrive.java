package no.runsafe.warpdrive;

import no.runsafe.framework.RunsafePlugin;
import no.runsafe.framework.event.player.IPlayerInteractEvent;
import no.runsafe.framework.server.RunsafeBlock;
import no.runsafe.framework.server.RunsafeLocation;
import no.runsafe.framework.server.RunsafeWorld;
import no.runsafe.framework.server.event.player.RunsafePlayerInteractEvent;
import no.runsafe.framework.server.player.RunsafePlayer;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.Sign;
import sun.misc.SignalHandler;

import java.util.HashMap;

public class WarpDrive extends RunsafePlugin implements IPlayerInteractEvent
{
    private HashMap<String, RunsafeLocation> signWarpLocations = new HashMap<String, RunsafeLocation>();

    public WarpDrive()
    {
        super();
    }

    @Override
    protected void PluginSetup()
    {
        //Add components
    }

    @Override
    public void OnPlayerInteractEvent(RunsafePlayerInteractEvent event)
    {
        RunsafePlayer thePlayer = event.getPlayer();
        Block theBlock = event.getBlock();

        if (theBlock != null)
        {
            BlockState theBlockState = theBlock.getState();

            if (theBlockState instanceof Sign)
            {
                Sign theSign = (Sign) theBlockState;

                if (theSign.getLine(0).equalsIgnoreCase("[Dynamic Warp]"))
                {
                    this.setRandomWarp(theSign, thePlayer);
                    event.setCancelled(true);
                }
            }
        }
    }

    private void setRandomWarp(Sign theSign, RunsafePlayer thePlayer)
    {
        String warpName = theSign.getLine(1);
        //!this.signWarpLocations.containsKey(signLocation)
        if (!this.signWarpLocations.containsKey(warpName))
        {
            int radius = Integer.parseInt(theSign.getLine(2));
            int boundingRadius = Integer.parseInt(theSign.getLine(3));

            boolean negX = (Math.random() * 100) > 50;
            boolean negY = (Math.random() * 100) > 50;

            double randomXB = (negX ? -1 : 1) * (((radius -boundingRadius) * Math.random()) + boundingRadius);
            double randomZB = (negY ? -1 : 1) * (((radius -boundingRadius) * Math.random()) + boundingRadius);

            thePlayer.sendMessage("X: " + randomXB);
            thePlayer.sendMessage("Z: " + randomZB);

            double randomX = randomXB + theSign.getX();
            double randomZ = randomZB + theSign.getZ();

            RunsafeWorld theWorld = new RunsafeWorld(theSign.getWorld());
            RunsafeLocation newLocation = new RunsafeLocation(theWorld, randomX, 60, randomZ);

            this.signWarpLocations.put(warpName, newLocation);

            this.safeTeleport(newLocation, thePlayer);
        }
        else
        {
            RunsafeLocation location = this.signWarpLocations.get(warpName);
            this.safeTeleport(location, thePlayer);
        }
    }

    public void safeTeleport(RunsafeLocation location, RunsafePlayer player)
    {
        int x = location.getBlockX();
        int y = Math.max(0, location.getBlockY());
        int origY = y;
        int z = location.getBlockZ();

        RunsafeWorld world = location.getWorld();

        byte free = 0;

        while (y <= world.getMaxHeight() + 1)
        {
            RunsafeBlock theBlock = world.getBlockAt(x, y, z);
            if (theBlock.canPassThrough())
            {
                free++;
            }
            else
            {
                free = 0;
            }

            if (free == 2)
            {
                if (y - 1 != origY || y == 1)
                {
                    location.setX(x + 0.5);
                    location.setY(y);
                    location.setZ(z + 0.5);

                    if (y <= 2 && world.getBlockAt(x,0,z).getTypeId() == Material.AIR.getId())
                    {
                        world.getBlockAt(x,0,z).setTypeId(Material.GLASS.getId());
                        location.setY(2);
                    }
                    player.setFallDistance(0F);
                    player.teleport(location);
                }
                else
                {
                    location.setX(location.getX() + 20);
                    location.setZ(location.getZ() + 20);
                    this.safeTeleport(location, player);
                }
                return;
            }

            y++;
        }
    }
}
