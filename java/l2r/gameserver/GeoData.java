/*
 * Copyright (C) 2004-2016 L2J Server
 * 
 * This file is part of L2J Server.
 * 
 * L2J Server is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * L2J Server is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package l2r.gameserver;

import java.nio.file.Files;
import java.nio.file.Path;

import l2r.Config;
import l2r.gameserver.data.xml.impl.DoorData;
import l2r.gameserver.model.L2Object;
import l2r.gameserver.model.L2World;
import l2r.gameserver.model.Location;
import l2r.gameserver.model.interfaces.ILocational;
import l2r.gameserver.util.GeoUtils;
import l2r.gameserver.util.LinePointIterator;
import l2r.gameserver.util.Util;

import com.l2jserver.geodriver.Cell;
import com.l2jserver.geodriver.GeoDriver;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Geodata.
 * @author -Nemesiss-, HorridoJoho
 */
public class GeoData
{
	private static final Logger LOGGER = LoggerFactory.getLogger(GeoData.class);
	private static final String FILE_NAME_FORMAT = "%d_%d.l2j";
	// private static final int ELEVATED_SEE_OVER_DISTANCE = 2;
	private static final double ELEVATED_SEE_OVER_DISTANCE_SQ = 3 * 3;
	private static final int MAX_SEE_OVER_HEIGHT = 48;
	private static final int SPAWN_Z_DELTA_LIMIT = 100;
	
	private final GeoDriver _driver = new GeoDriver();
	
	protected GeoData()
	{
		int loadedRegions = 0;
		try
		{
			for (int regionX = L2World.TILE_X_MIN; regionX <= L2World.TILE_X_MAX; regionX++)
			{
				for (int regionY = L2World.TILE_Y_MIN; regionY <= L2World.TILE_Y_MAX; regionY++)
				{
					final Path geoFilePath = Config.GEODATA_PATH.resolve(String.format(FILE_NAME_FORMAT, regionX, regionY));
					final Boolean loadFile = Config.GEODATA_REGIONS.get(regionX + "_" + regionY);
					if (loadFile != null)
					{
						if (loadFile)
						{
							LOGGER.info("{}: Loading {}...", getClass().getSimpleName(), geoFilePath.getFileName());
							_driver.loadRegion(geoFilePath, regionX, regionY);
							loadedRegions++;
						}
					}
					else if (Config.TRY_LOAD_UNSPECIFIED_REGIONS && Files.exists(geoFilePath))
					{
						try
						{
							LOGGER.info("{}: Loading {}...", getClass().getSimpleName(), geoFilePath.getFileName());
							_driver.loadRegion(geoFilePath, regionX, regionY);
							loadedRegions++;
						}
						catch (Exception e)
						{
							LOGGER.warn(getClass().getSimpleName() + ": Failed to load {}!", geoFilePath.getFileName(), e);
						}
					}
				}
			}
		}
		catch (Exception e)
		{
			LOGGER.error("{}: Failed to load geodata!", e);
			System.exit(1);
		}
		
		LOGGER.info("{}: Loaded {} regions.", getClass().getSimpleName(), loadedRegions);
	}
	
	public boolean hasGeoPos(int geoX, int geoY)
	{
		return _driver.hasGeoPos(geoX, geoY);
	}
	
	public boolean checkNearestNswe(int geoX, int geoY, int worldZ, int nswe)
	{
		return _driver.checkNearestNswe(geoX, geoY, worldZ, nswe);
	}
	
	public boolean checkNearestNsweAntiCornerCut(int geoX, int geoY, int worldZ, int nswe)
	{
		boolean can = checkNearestNswe(geoX, geoY, worldZ, nswe);
		if (can && ((nswe & Cell.NSWE_NORTH_EAST) == Cell.NSWE_NORTH_EAST))
		{
			can = checkNearestNswe(geoX, geoY - 1, worldZ, Cell.NSWE_EAST) && checkNearestNswe(geoX + 1, geoY, worldZ, Cell.NSWE_NORTH);
		}
		
		if (can && ((nswe & Cell.NSWE_NORTH_WEST) == Cell.NSWE_NORTH_WEST))
		{
			can = checkNearestNswe(geoX, geoY - 1, worldZ, Cell.NSWE_WEST) && checkNearestNswe(geoX, geoY - 1, worldZ, Cell.NSWE_NORTH);
		}
		
		if (can && ((nswe & Cell.NSWE_SOUTH_EAST) == Cell.NSWE_SOUTH_EAST))
		{
			can = checkNearestNswe(geoX, geoY + 1, worldZ, Cell.NSWE_EAST) && checkNearestNswe(geoX + 1, geoY, worldZ, Cell.NSWE_SOUTH);
		}
		
		if (can && ((nswe & Cell.NSWE_SOUTH_WEST) == Cell.NSWE_SOUTH_WEST))
		{
			can = checkNearestNswe(geoX, geoY + 1, worldZ, Cell.NSWE_WEST) && checkNearestNswe(geoX - 1, geoY, worldZ, Cell.NSWE_SOUTH);
		}
		
		return can;
	}
	
	public int getNearestZ(int geoX, int geoY, int worldZ)
	{
		return _driver.getNearestZ(geoX, geoY, worldZ);
	}
	
	public int getNextLowerZ(int geoX, int geoY, int worldZ)
	{
		return _driver.getNextLowerZ(geoX, geoY, worldZ);
	}
	
	public int getNextHigherZ(int geoX, int geoY, int worldZ)
	{
		return _driver.getNextHigherZ(geoX, geoY, worldZ);
	}
	
	public int getGeoX(int worldX)
	{
		return _driver.getGeoX(worldX);
	}
	
	public int getGeoY(int worldY)
	{
		return _driver.getGeoY(worldY);
	}
	
	public int getWorldX(int geoX)
	{
		return _driver.getWorldX(geoX);
	}
	
	public int getWorldY(int geoY)
	{
		return _driver.getWorldY(geoY);
	}
	
	// ///////////////////
	// L2J METHODS
	/**
	 * Gets the height.
	 * @param x the x coordinate
	 * @param y the y coordinate
	 * @param z the z coordinate
	 * @return the height
	 */
	public int getHeight(int x, int y, int z)
	{
		return getNearestZ(getGeoX(x), getGeoY(y), z);
	}
	
	/**
	 * Gets the spawn height. This method is used to coorect Z for spawning and<br>
	 * movement.
	 * @param x the x coordinate
	 * @param y the y coordinate
	 * @param z the the z coordinate
	 * @return the spawn height
	 */
	public int getSpawnHeight(int x, int y, int z)
	{
		final int geoX = getGeoX(x);
		final int geoY = getGeoY(y);
		
		if (!hasGeoPos(geoX, geoY))
		{
			return z;
		}
		
		// this Z correction can not increase Z by more than SPAWN_Z_DELTA_LIMIT units and can decrease Z an unlimited amount
		int nearestZ = getNearestZ(geoX, geoY, z);
		if ((nearestZ > z) && ((nearestZ - z) > SPAWN_Z_DELTA_LIMIT))
		{
			// here the nearest Z was more than SPAWN_Z_DELTA_LIMIT above Z, so we use the next lower z instead
			return getNextLowerZ(geoX, geoY, z);
		}
		return nearestZ;
	}
	
	/**
	 * Gets the spawn height.
	 * @param location the location
	 * @return the spawn height
	 */
	public int getSpawnHeight(Location location)
	{
		return getSpawnHeight(location.getX(), location.getY(), location.getZ());
	}
	
	/**
	 * Can see target. Doors as target always return true. Checks doors between.
	 * @param cha the character
	 * @param target the target
	 * @return {@code true} if the character can see the target (LOS), {@code false} otherwise
	 */
	public boolean canSeeTarget(L2Object cha, L2Object target)
	{
		if (target == null)
		{
			return false;
		}
		if (target.isDoor())
		{
			return true;
		}
		return canSeeTarget(cha.getX(), cha.getY(), cha.getZ(), cha.getInstanceId(), target.getX(), target.getY(), target.getZ(), target.getInstanceId());
	}
	
	/**
	 * Can see target. Checks doors between.
	 * @param cha the character
	 * @param worldPosition the world position
	 * @return {@code true} if the character can see the target at the given world position, {@code false} otherwise
	 */
	public boolean canSeeTarget(L2Object cha, ILocational worldPosition)
	{
		return canSeeTarget(cha.getX(), cha.getY(), cha.getZ(), cha.getInstanceId(), worldPosition.getX(), worldPosition.getY(), worldPosition.getZ());
	}
	
	/**
	 * Can see target. Checks doors between.
	 * @param x the x coordinate
	 * @param y the y coordinate
	 * @param z the z coordinate
	 * @param instanceId
	 * @param tx the target's x coordinate
	 * @param ty the target's y coordinate
	 * @param tz the target's z coordinate
	 * @param tInstanceId the target's instanceId
	 * @return
	 */
	public boolean canSeeTarget(int x, int y, int z, int instanceId, int tx, int ty, int tz, int tInstanceId)
	{
		if ((instanceId != tInstanceId))
		{
			return false;
		}
		return canSeeTarget(x, y, z, instanceId, tx, ty, tz);
	}
	
	/**
	 * Can see target. Checks doors between.
	 * @param x the x coordinate
	 * @param y the y coordinate
	 * @param z the z coordinate
	 * @param instanceId
	 * @param tx the target's x coordinate
	 * @param ty the target's y coordinate
	 * @param tz the target's z coordinate
	 * @return {@code true} if there is line of sight between the given coordinate sets, {@code false} otherwise
	 */
	public boolean canSeeTarget(int x, int y, int z, int instanceId, int tx, int ty, int tz)
	{
		if (DoorData.getInstance().checkIfDoorsBetween(x, y, z, tx, ty, tz, instanceId, true))
		{
			return false;
		}
		return canSeeTarget(x, y, z, tx, ty, tz);
	}
	
	private int getLosGeoZ(int prevX, int prevY, int prevGeoZ, int curX, int curY, int nswe)
	{
		if ((((nswe & Cell.NSWE_NORTH) != 0) && ((nswe & Cell.NSWE_SOUTH) != 0)) || (((nswe & Cell.NSWE_WEST) != 0) && ((nswe & Cell.NSWE_EAST) != 0)))
		{
			throw new RuntimeException("Multiple directions!");
		}
		
		if (checkNearestNsweAntiCornerCut(prevX, prevY, prevGeoZ, nswe))
		{
			return getNearestZ(curX, curY, prevGeoZ);
		}
		return getNextHigherZ(curX, curY, prevGeoZ);
	}
	
	/**
	 * Can see target. Does not check doors between.
	 * @param x the x coordinate
	 * @param y the y coordinate
	 * @param z the z coordinate
	 * @param tx the target's x coordinate
	 * @param ty the target's y coordinate
	 * @param tz the target's z coordinate
	 * @return {@code true} if there is line of sight between the given coordinate sets, {@code false} otherwise
	 */
	public boolean canSeeTarget(int x, int y, int z, int tx, int ty, int tz)
	{
		int geoX = getGeoX(x);
		int geoY = getGeoY(y);
		int tGeoX = getGeoX(tx);
		int tGeoY = getGeoY(ty);
		
		z = getNearestZ(geoX, geoY, z);
		tz = getNearestZ(tGeoX, tGeoY, tz);
		
		// fastpath
		if ((geoX == tGeoX) && (geoY == tGeoY))
		{
			if (hasGeoPos(tGeoX, tGeoY))
			{
				return z == tz;
			}
			
			return true;
		}
		
		if (tz > z)
		{
			int tmp = tx;
			tx = x;
			x = tmp;
			
			tmp = ty;
			ty = y;
			y = tmp;
			
			tmp = tz;
			tz = z;
			z = tmp;
			
			tmp = tGeoX;
			tGeoX = geoX;
			geoX = tmp;
			
			tmp = tGeoY;
			tGeoY = geoY;
			geoY = tmp;
		}
		
		final int dz = z - tz;
		
		final double fullDistanceSq = Util.calculateDistance(geoX, geoY, 0, tGeoX, tGeoY, 0, false, true);
		final double secondSeeOverDistancePoint = fullDistanceSq - ELEVATED_SEE_OVER_DISTANCE_SQ;
		
		// LinePointIterator3D pointIter = new LinePointIterator3D(geoX, geoY, z, tGeoX, tGeoY, tz);
		LinePointIterator pointIter = new LinePointIterator(geoX, geoY, tGeoX, tGeoY);
		// first point is guaranteed to be available, skip it, we can always see our own position
		pointIter.next();
		int prevX = pointIter.x();
		int prevY = pointIter.y();
		// int prevBeeZ = pointIter.z();
		int prevBeeZ = z;
		int prevGeoZ = prevBeeZ;
		// int pointIndex = 0;
		while (pointIter.next())
		{
			int curX = pointIter.x();
			int curY = pointIter.y();
			
			// if ((curX == prevX) && (curY == prevY))
			// {
			// continue;
			// }
			
			double curDistanceSq = Util.calculateDistance(geoX, geoY, 0, curX, curY, 0, false, true);
			int curBeeZ = z - (int) (dz * ((1.0 * curDistanceSq) / fullDistanceSq));
			int curGeoZ = prevGeoZ;
			
			// check if the position has geodata
			if (hasGeoPos(curX, curY))
			{
				int nswe = GeoUtils.computeNswe(prevX, prevY, curX, curY);
				curGeoZ = getLosGeoZ(prevX, prevY, prevGeoZ, curX, curY, nswe);
				int maxHeight = curBeeZ + MAX_SEE_OVER_HEIGHT;
				// only allow shooting over fences when oponents are far enough away from each other
				if (fullDistanceSq > ELEVATED_SEE_OVER_DISTANCE_SQ)
				{
					if (curDistanceSq <= ELEVATED_SEE_OVER_DISTANCE_SQ)
					{
						maxHeight = z + MAX_SEE_OVER_HEIGHT;
					}
					else if (curDistanceSq >= secondSeeOverDistancePoint)
					{
						maxHeight = tz + MAX_SEE_OVER_HEIGHT;
					}
				}
				
				boolean canSeeThrough = false;
				if (curGeoZ <= maxHeight)
				{
					if ((nswe & Cell.NSWE_NORTH_EAST) == Cell.NSWE_NORTH_EAST)
					{
						int northGeoZ = getLosGeoZ(prevX, prevY, prevGeoZ, prevX, prevY - 1, Cell.NSWE_EAST);
						int eastGeoZ = getLosGeoZ(prevX, prevY, prevGeoZ, prevX + 1, prevY, Cell.NSWE_NORTH);
						canSeeThrough = (northGeoZ <= maxHeight) && (eastGeoZ <= maxHeight);
					}
					else if ((nswe & Cell.NSWE_NORTH_WEST) == Cell.NSWE_NORTH_WEST)
					{
						int northGeoZ = getLosGeoZ(prevX, prevY, prevGeoZ, prevX, prevY - 1, Cell.NSWE_WEST);
						int westGeoZ = getLosGeoZ(prevX, prevY, prevGeoZ, prevX - 1, prevY, Cell.NSWE_NORTH);
						canSeeThrough = (northGeoZ <= maxHeight) && (westGeoZ <= maxHeight);
					}
					else if ((nswe & Cell.NSWE_SOUTH_EAST) == Cell.NSWE_SOUTH_EAST)
					{
						int southGeoZ = getLosGeoZ(prevX, prevY, prevGeoZ, prevX, prevY + 1, Cell.NSWE_EAST);
						int eastGeoZ = getLosGeoZ(prevX, prevY, prevGeoZ, prevX + 1, prevY, Cell.NSWE_SOUTH);
						canSeeThrough = (southGeoZ <= maxHeight) && (eastGeoZ <= maxHeight);
					}
					else if ((nswe & Cell.NSWE_SOUTH_WEST) == Cell.NSWE_SOUTH_WEST)
					{
						int southGeoZ = getLosGeoZ(prevX, prevY, prevGeoZ, prevX, prevY + 1, Cell.NSWE_WEST);
						int westGeoZ = getLosGeoZ(prevX, prevY, prevGeoZ, prevX - 1, prevY, Cell.NSWE_SOUTH);
						canSeeThrough = (southGeoZ <= maxHeight) && (westGeoZ <= maxHeight);
					}
					else
					{
						canSeeThrough = true;
					}
				}
				
				if (!canSeeThrough)
				{
					return false;
				}
			}
			
			prevX = curX;
			prevY = curY;
			prevBeeZ = curBeeZ;
			prevGeoZ = curGeoZ;
			// ++pointIndex;
		}
		
		// the traced end Z must be the same than the targets Z
		return prevGeoZ == tz;
	}
	
	/**
	 * Verifies if the is a path between origin's location and destination, if not returns the closest location.
	 * @param origin the origin
	 * @param destination the destination
	 * @return the destination if there is a path or the closes location
	 */
	public Location moveCheck(ILocational origin, ILocational destination)
	{
		return moveCheck(origin.getX(), origin.getY(), origin.getZ(), destination.getX(), destination.getY(), destination.getZ(), origin.getInstanceId());
	}
	
	/**
	 * Move check.
	 * @param x the x coordinate
	 * @param y the y coordinate
	 * @param z the z coordinate
	 * @param tx the target's x coordinate
	 * @param ty the target's y coordinate
	 * @param tz the target's z coordinate
	 * @param instanceId the instance id
	 * @return the last Location (x,y,z) where player can walk - just before wall
	 */
	public Location moveCheck(int x, int y, int z, int tx, int ty, int tz, int instanceId)
	{
		int geoX = getGeoX(x);
		int geoY = getGeoY(y);
		z = getNearestZ(geoX, geoY, z);
		int tGeoX = getGeoX(tx);
		int tGeoY = getGeoY(ty);
		tz = getNearestZ(tGeoX, tGeoY, tz);
		
		if (DoorData.getInstance().checkIfDoorsBetween(x, y, z, tx, ty, tz, instanceId, false))
		{
			return new Location(x, y, getHeight(x, y, z));
		}
		
		LinePointIterator pointIter = new LinePointIterator(geoX, geoY, tGeoX, tGeoY);
		// first point is guaranteed to be available
		pointIter.next();
		int prevX = pointIter.x();
		int prevY = pointIter.y();
		int prevZ = z;
		
		while (pointIter.next())
		{
			int curX = pointIter.x();
			int curY = pointIter.y();
			int curZ = getNearestZ(curX, curY, prevZ);
			
			if (hasGeoPos(prevX, prevY))
			{
				int nswe = GeoUtils.computeNswe(prevX, prevY, curX, curY);
				if (!checkNearestNsweAntiCornerCut(prevX, prevY, prevZ, nswe))
				{
					// can't move, return previous location
					return new Location(getWorldX(prevX), getWorldY(prevY), prevZ);
				}
			}
			
			prevX = curX;
			prevY = curY;
			prevZ = curZ;
		}
		
		if (hasGeoPos(prevX, prevY) && (prevZ != tz))
		{
			// different floors, return start location
			return new Location(x, y, z);
		}
		
		return new Location(tx, ty, tz);
	}
	
	/**
	 * Checks if its possible to move from one location to another.
	 * @param fromX the X coordinate to start checking from
	 * @param fromY the Y coordinate to start checking from
	 * @param fromZ the Z coordinate to start checking from
	 * @param toX the X coordinate to end checking at
	 * @param toY the Y coordinate to end checking at
	 * @param toZ the Z coordinate to end checking at
	 * @param instanceId the instance ID
	 * @return {@code true} if the character at start coordinates can move to end coordinates, {@code false} otherwise
	 */
	public boolean canMove(int fromX, int fromY, int fromZ, int toX, int toY, int toZ, int instanceId)
	{
		int geoX = getGeoX(fromX);
		int geoY = getGeoY(fromY);
		fromZ = getNearestZ(geoX, geoY, fromZ);
		int tGeoX = getGeoX(toX);
		int tGeoY = getGeoY(toY);
		toZ = getNearestZ(tGeoX, tGeoY, toZ);
		
		if (DoorData.getInstance().checkIfDoorsBetween(fromX, fromY, fromZ, toX, toY, toZ, instanceId, false))
		{
			return false;
		}
		
		LinePointIterator pointIter = new LinePointIterator(geoX, geoY, tGeoX, tGeoY);
		// first point is guaranteed to be available
		pointIter.next();
		int prevX = pointIter.x();
		int prevY = pointIter.y();
		int prevZ = fromZ;
		
		while (pointIter.next())
		{
			int curX = pointIter.x();
			int curY = pointIter.y();
			int curZ = getNearestZ(curX, curY, prevZ);
			
			if (hasGeoPos(prevX, prevY))
			{
				int nswe = GeoUtils.computeNswe(prevX, prevY, curX, curY);
				if (!checkNearestNsweAntiCornerCut(prevX, prevY, prevZ, nswe))
				{
					return false;
				}
			}
			
			prevX = curX;
			prevY = curY;
			prevZ = curZ;
		}
		
		if (hasGeoPos(prevX, prevY) && (prevZ != toZ))
		{
			// different floors
			return false;
		}
		
		return true;
	}
	
	public int traceTerrainZ(int x, int y, int z, int tx, int ty)
	{
		int geoX = getGeoX(x);
		int geoY = getGeoY(y);
		z = getNearestZ(geoX, geoY, z);
		int tGeoX = getGeoX(tx);
		int tGeoY = getGeoY(ty);
		
		LinePointIterator pointIter = new LinePointIterator(geoX, geoY, tGeoX, tGeoY);
		// first point is guaranteed to be available
		pointIter.next();
		int prevZ = z;
		
		while (pointIter.next())
		{
			int curX = pointIter.x();
			int curY = pointIter.y();
			int curZ = getNearestZ(curX, curY, prevZ);
			
			prevZ = curZ;
		}
		
		return prevZ;
	}
	
	/**
	 * Checks if its possible to move from one location to another.
	 * @param from the {@code ILocational} to start checking from
	 * @param toX the X coordinate to end checking at
	 * @param toY the Y coordinate to end checking at
	 * @param toZ the Z coordinate to end checking at
	 * @return {@code true} if the character at start coordinates can move to end coordinates, {@code false} otherwise
	 */
	public boolean canMove(ILocational from, int toX, int toY, int toZ)
	{
		return canMove(from.getX(), from.getY(), from.getZ(), toX, toY, toZ, from.getInstanceId());
	}
	
	/**
	 * Checks if its possible to move from one location to another.
	 * @param from the {@code ILocational} to start checking from
	 * @param to the {@code ILocational} to end checking at
	 * @return {@code true} if the character at start coordinates can move to end coordinates, {@code false} otherwise
	 */
	public boolean canMove(ILocational from, ILocational to)
	{
		return canMove(from, to.getX(), to.getY(), to.getZ());
	}
	
	/**
	 * Checks the specified position for available geodata.
	 * @param x the X coordinate
	 * @param y the Y coordinate
	 * @return {@code true} if there is geodata for the given coordinates, {@code false} otherwise
	 */
	public boolean hasGeo(int x, int y)
	{
		return hasGeoPos(getGeoX(x), getGeoY(y));
	}
	
	public static GeoData getInstance()
	{
		return SingletonHolder._instance;
	}
	
	private static class SingletonHolder
	{
		protected final static GeoData _instance = new GeoData();
	}
}