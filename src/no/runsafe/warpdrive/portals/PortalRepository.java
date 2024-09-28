package no.runsafe.warpdrive.portals;

import no.runsafe.framework.api.database.IRow;
import no.runsafe.framework.api.database.ISchemaUpdate;
import no.runsafe.framework.api.database.Repository;
import no.runsafe.framework.api.database.SchemaUpdate;
import no.runsafe.framework.internal.vector.Region3D;
import no.runsafe.warpdrive.WarpDrive;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

public class PortalRepository extends Repository
{
	@Nonnull
	@Override
	public String getTableName()
	{
		return "warpdrive_portals";
	}

	public List<PortalWarp> getPortalWarps()
	{
		List<PortalWarp> warps = new ArrayList<>();

		for (IRow row : database.query("SELECT * FROM warpdrive_portals"))
		{
			String portalID = row.String("ID");
			try
			{
				WarpDrive.debug.debugFine("%s gives: %s", row.String("portal_field"), Region3D.fromString(row.String("portal_field")));

				if (row.String("region") != null)
					WarpDrive.debug.debugFine("Portal %s has region: %s", portalID, row.String("region"));

				warps.add(new PortalWarp(
					portalID,
					row.Location(),
					row.Location("destWorld", "destX", "destY", "destZ", "destYaw", "destPitch"),
					PortalType.getPortalType(row.Integer("type")),
					(row.Integer("radius") == null ? 0 : row.Integer("radius")),
					row.String("permission"),
					Region3D.fromString(row.String("portal_field")),
					row.String("region")
				));
			}
			catch (NullPointerException e)
			{
				deleteWarp(portalID);
			}
		}
		return warps;
	}

	public void deleteWarp(String warpID)
	{
		database.execute("DELETE FROM warpdrive_portals WHERE ID = ?", warpID);
	}

	public void setWarp(PortalWarp warp)
	{
		database.execute(
			"INSERT INTO warpdrive_portals (ID, world, x, y, z, destWorld, destX, destY, destZ, destYaw, destPitch, radius, permission, portal_field, region) " +
				"VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?) " +
				"ON DUPLICATE KEY UPDATE x=VALUES(x), y=VALUES(y), z=VALUES(z), destWorld=VALUES(destWorld), " +
				"destX=VALUES(destX), destY=VALUES(destY), destZ=VALUES(destZ), destYaw=VALUES(destYaw), destPitch=VALUES(destPitch), " +
				"radius=VALUES(radius), permission=VALUES(permission), portal_field=VALUES(portal_field), region=VALUES(region)",
			warp.getID(), warp.getWorldName(), warp.getX(), warp.getY(), warp.getZ(), warp.getDestinationWorldName(),
			warp.getDestinationX(), warp.getDestinationY(), warp.getDestinationZ(), warp.getDestinationYaw(),
			warp.getDestinationPitch(), null, warp.getPermission(), warp.getRegion() == null ? null : warp.getRegion().toString(),
			warp.getEnterRegion()
		);
	}

	@Nonnull
	@Override
	public ISchemaUpdate getSchemaUpdateQueries()
	{
		ISchemaUpdate update = new SchemaUpdate();

		update.addQueries(
			"CREATE TABLE `warpdrive_portals` (" +
				"`ID` VARCHAR(50) NOT NULL," +
				"`permission` VARCHAR(255) NOT NULL DEFAULT ''," +
				"`type` TINYINT NOT NULL DEFAULT '0'," +
				"`world` VARCHAR(255) NOT NULL," +
				"`x` DOUBLE NOT NULL," +
				"`y` DOUBLE NOT NULL," +
				"`z` DOUBLE NOT NULL," +
				"`destWorld` VARCHAR(255) NOT NULL," +
				"`destX` DOUBLE NULL," +
				"`destY` DOUBLE NULL," +
				"`destZ` DOUBLE NULL," +
				"`destYaw` DOUBLE NULL," +
				"`destPitch` DOUBLE NULL," +
				"PRIMARY KEY (`ID`)" +
				")"
		);

		update.addQueries("ALTER TABLE `warpdrive_portals`" +
			"ADD COLUMN `radius` INT UNSIGNED NULL DEFAULT NULL AFTER `destPitch`;");

		update.addQueries("ALTER TABLE `warpdrive_portals`" +
			"ADD COLUMN `portal_field` VARCHAR(255) NULL;");

		update.addQueries("ALTER TABLE `warpdrive_portals`" +
			"ADD COLUMN `region` VARCHAR(50) NULL DEFAULT NULL AFTER `portal_field`;");

		update.addQueries("ALTER TABLE `warpdrive_portals`" +
				"CHANGE COLUMN `permission` `permission` VARCHAR(255) NULL DEFAULT NULL AFTER `ID`;");

		return update;
	}
}
