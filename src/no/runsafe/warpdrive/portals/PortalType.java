package no.runsafe.warpdrive.portals;

public enum PortalType
{
	NORMAL,
	RANDOM_SURFACE,
	RANDOM_CAVE,
	RANDOM_RADIUS;

	public static PortalType getPortalType(int id)
	{
		for (PortalType type : PortalType.values())
			if (type.ordinal() == id)
				return type;

		return PortalType.NORMAL;
	}
}
