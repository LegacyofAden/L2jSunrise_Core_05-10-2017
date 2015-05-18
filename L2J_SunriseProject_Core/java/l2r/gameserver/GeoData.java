/*
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 */
package l2r.gameserver;

import java.util.Vector;

import l2r.Config;
import l2r.gameserver.data.xml.impl.DoorData;
import l2r.gameserver.model.L2Object;
import l2r.gameserver.model.Location;
import l2r.gameserver.model.actor.L2Character;
import l2r.geoserver.geodata.GeoEngine;
import l2r.geoserver.geodata.PathFindBuffers;
import l2r.geoserver.model.GeoCollision;
import l2r.geoserver.model.MoveTrick;
import l2r.util.Rnd;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @Author: Death
 * @Date: 23/11/2007
 * @Time: 12:13:11
 */
public class GeoData
{
	final static Logger _log = LoggerFactory.getLogger(GeoData.class);
	
	private static GeoData instance;
	
	public static final int seeUp = 16;
	
	private GeoEngine geoEngine;
	
	public static GeoData getInstance()
	{
		if (instance == null)
		{
			try
			{
				new GeoData();
			}
			catch (Exception e)
			{
				e.printStackTrace();
				System.out.println("Can't init geoclient.");
				System.exit(0);
			}
		}
		return instance;
	}
	
	public GeoData()
	{
		instance = this;
		
		if (Config.GEODATA)
		{
			initLocal();
		}
		else
		{
			initFake();
		}
	}
	
	public void initFake()
	{
		_log.info("GeoData: Disabled");
	}
	
	public void initLocal()
	{
		PathFindBuffers.initBuffers("8x100;8x128;8x192;4x256;2x320;2x384;1x500");
		GeoEngine.loadGeo();
		geoEngine = new GeoEngine();
		_log.info("GeoData: GeoEngine Local Started");
	}
	
	public Vector<Location> pathFind(int x, int y, int z, Location pos)
	{
		if (Config.GEODATA)
		{
			return geoEngine.pathFind(x, y, z, pos.getX(), pos.getY(), pos.getZ());
		}
		return new Vector<>();
	}
	
	public Vector<Location> pathFind(int x, int y, int z, int tx, int ty, int tz)
	{
		if (Config.GEODATA)
		{
			return geoEngine.pathFind(x, y, z, tx, ty, tz);
		}
		return new Vector<>();
	}
	
	public boolean canSeeTarget(L2Character actor, L2Character target)
	{
		if ((actor == null) || (target == null))
		{
			return true;
		}
		
		if (actor == target)
		{
			return true;
		}
		
		if (DoorData.getInstance().checkIfDoorsBetween(actor.getX(), actor.getY(), actor.getZ(), target.getX(), target.getY(), target.getZ(), actor.getInstanceId()) && !target.isDoor())
		{
			return false;
		}
		
		return canSeeTarget(actor.getX(), actor.getY(), actor.getZ(), target.getX(), target.getY(), target.getZ(), actor.isFlying(), actor.getTemplate().getCollisionHeight(), target.getTemplate().getCollisionHeight());
	}
	
	public boolean canSeeTarget(L2Character actor, L2Object target)
	{
		if ((actor == null) || (target == null))
		{
			return true;
		}
		
		if ((target instanceof L2Character) && (actor == target))
		{
			return true;
		}
		
		if (DoorData.getInstance().checkIfDoorsBetween(actor.getX(), actor.getY(), actor.getZ(), target.getX(), target.getY(), target.getZ(), actor.getInstanceId()) && !target.isDoor())
		{
			return false;
		}
		
		return canSeeTarget(actor.getX(), actor.getY(), actor.getZ(), target.getX(), target.getY(), target.getZ(), actor.isFlying(), actor.getTemplate().getCollisionHeight(), 16);
	}
	
	public boolean canSeeTarget(L2Character actor, Location pos)
	{
		if ((actor == null) || (pos == null))
		{
			return true;
		}
		
		if (DoorData.getInstance().checkIfDoorsBetween(actor.getX(), actor.getY(), actor.getZ(), pos.getX(), pos.getY(), pos.getZ(), actor.getInstanceId()))
		{
			return false;
		}
		
		return canSeeTarget(actor.getX(), actor.getY(), actor.getZ(), pos.getX(), pos.getY(), pos.getZ(), actor.isFlying(), actor.getTemplate().getCollisionHeight(), 16);
	}
	
	public boolean canSeeTarget(L2Character actor, int tx, int ty, int tz, boolean inAir)
	{
		if (actor == null)
		{
			return true;
		}
		
		if (DoorData.getInstance().checkIfDoorsBetween(actor.getX(), actor.getY(), actor.getZ(), tx, ty, tz, actor.getInstanceId()))
		{
			return false;
		}
		
		return canSeeTarget(actor.getX(), actor.getY(), actor.getZ(), tx, ty, tz, inAir, actor.getTemplate().getCollisionHeight(), 16);
	}
	
	public Location moveInWaterCheck(L2Character actor, Location toPos)
	{
		if (actor == null)
		{
			return null;
		}
		
		if (DoorData.getInstance().checkIfDoorsBetween(actor.getX(), actor.getY(), actor.getZ(), toPos.getX(), toPos.getY(), toPos.getZ(), actor.getInstanceId()))
		{
			return null;
		}
		
		return moveInWaterCheck(actor.getX(), actor.getY(), actor.getZ(), toPos.getX(), toPos.getY(), toPos.getZ(), 0, 0);
	}
	
	private boolean canSeeTarget(int x, int y, int z, int tx, int ty, int tz, boolean inAir, int colHeightActor, int colHeightTarget)
	{
		if (Config.GEODATA)
		{
			return geoEngine.canSeeTarget(x, y, z, tx, ty, tz, inAir, colHeightActor, colHeightTarget);
		}
		return true;
	}
	
	public int getHeight(int x, int y, int z)
	{
		if (Config.GEODATA)
		{
			return geoEngine.getHeight(x, y, z);
		}
		return z;
	}
	
	public int getHeight(Location loc)
	{
		return getHeight(loc.getX(), loc.getY(), loc.getZ());
	}
	
	public Location moveCheckWithoutDoors(Location fromPos, Location toPos, boolean returnPrev)
	{
		return moveCheck(fromPos.getX(), fromPos.getY(), fromPos.getZ(), toPos.getX(), toPos.getY(), toPos.getZ(), returnPrev);
	}
	
	public Location moveCheck(L2Character actor, Location toPos, boolean returnPrev)
	{
		if ((actor == null) || DoorData.getInstance().checkIfDoorsBetween(actor.getX(), actor.getY(), actor.getZ(), toPos.getX(), toPos.getY(), toPos.getZ(), actor.getInstanceId()))
		{
			return null;
		}
		
		return moveCheck(actor.getX(), actor.getY(), actor.getZ(), toPos.getX(), toPos.getY(), toPos.getZ(), returnPrev);
	}
	
	public Location moveCheck(int x, int y, int z, int tx, int ty, int tz, boolean returnPrev)
	{
		if (Config.GEODATA)
		{
			return geoEngine.moveCheck(x, y, z, tx, ty, tz, returnPrev);
		}
		return new Location(tx, ty, tz);
	}
	
	public boolean canMoveToCoord(L2Character actor, Location pos, boolean returnPrev)
	{
		if (DoorData.getInstance().checkIfDoorsBetween(actor.getX(), actor.getY(), actor.getZ(), pos.getX(), pos.getY(), pos.getZ(), actor.getInstanceId()))
		{
			return false;
		}
		
		return canMoveToCoord(actor.getX(), actor.getY(), actor.getZ(), pos.getX(), pos.getY(), pos.getZ(), returnPrev);
	}
	
	public boolean canMoveToCoord(L2Character actor, int tx, int ty, int tz, boolean returnPrev)
	{
		if (DoorData.getInstance().checkIfDoorsBetween(actor.getX(), actor.getY(), actor.getZ(), tx, ty, tz, actor.getInstanceId()))
		{
			return false;
		}
		
		return canMoveToCoord(actor.getX(), actor.getY(), actor.getZ(), tx, ty, tz, returnPrev);
	}
	
	public boolean canMoveToCoord(int x, int y, int z, int tx, int ty, int tz, boolean returnPrev)
	{
		if (Config.GEODATA)
		{
			return geoEngine.canMoveToCoord(x, y, z, tx, ty, tz, returnPrev);
		}
		return true;
	}
	
	public MoveTrick[] canMoveAdvanced(L2Character actor, Location pos, boolean returnPrev)
	{
		if (DoorData.getInstance().checkIfDoorsBetween(actor.getX(), actor.getY(), actor.getZ(), pos.getX(), pos.getY(), pos.getZ(), actor.getInstanceId()))
		{
			return null;
		}
		
		return canMoveAdvanced(actor.getX(), actor.getY(), actor.getZ(), pos.getX(), pos.getY(), pos.getZ(), returnPrev);
	}
	
	private MoveTrick[] canMoveAdvanced(int x, int y, int z, int tx, int ty, int tz, boolean returnPrev)
	{
		if (Config.GEODATA)
		{
			return geoEngine.canMoveAdvanced(x, y, z, tx, ty, tz, returnPrev);
		}
		int dx1 = x - tx;
		int dy1 = y - ty;
		int dist = (int) Math.sqrt((dx1 * dx1) + (dy1 * dy1));
		MoveTrick[] result =
		{
			new MoveTrick(dist, tz)
		};
		return result;
	}
	
	public Location moveCheckForAI(L2Object cha, L2Object target)
	{
		return moveCheckForAI(new Location(cha.getX(), cha.getY(), cha.getZ()), new Location(target.getX(), target.getY(), target.getZ()));
	}
	
	public Location moveCheckForAI(Location loc1, Location loc2)
	{
		if (Config.GEODATA)
		{
			return geoEngine.moveCheckForAI(loc1, loc2);
		}
		return loc2;
	}
	
	public short getNSWE(int x, int y, int z)
	{
		if (Config.GEODATA)
		{
			return geoEngine.getNSWE(x, y, z);
		}
		return 15; // ALL
	}
	
	public MoveTrick[] canMoveToCoordWithCollision(int x, int y, int z, int tx, int ty, int tz, boolean returnPrev)
	{
		if (Config.GEODATA)
		{
			return geoEngine.canMoveToTargetWithCollision(x, y, z, tx, ty, tz, returnPrev, false);
		}
		int dx1 = x - tx;
		int dy1 = y - ty;
		int dist = (int) Math.sqrt((dx1 * dx1) + (dy1 * dy1));
		MoveTrick[] result =
		{
			new MoveTrick(dist, tz)
		};
		return result;
	}
	
	private Location moveInWaterCheck(int x, int y, int z, int tx, int ty, int tz, int colHeightActor, int colHeightTarget)
	{
		if (Config.GEODATA)
		{
			return geoEngine.moveInWaterCheck(x, y, z, tx, ty, tz, colHeightActor, colHeightTarget);
		}
		return new Location(tx, ty, tz);
	}
	
	public Location moveCheckInAir(int x, int y, int z, int tx, int ty, int tz, double collision)
	{
		if (Config.GEODATA)
		{
			return geoEngine.moveCheckInAir(x, y, z, tx, ty, tz, collision);
		}
		return new Location(tx, ty, tz);
	}
	
	/**
	 * @param x
	 * @param y
	 * @return Geo Block Type
	 */
	public short getType(int x, int y)
	{
		if (Config.GEODATA)
		{
			return geoEngine.getType(x, y);
		}
		return 0;
	}
	
	public static Location coordsRandomize(L2Character actor, int x, int y, int z, int heading, int radius_min, int radius_max, boolean GeoZCorrect)
	{
		if ((radius_max == 0) || (radius_max < radius_min))
		{
			return new Location(x, y, z, heading);
		}
		
		if (actor.isFlying())
		{
			return new Location(x, y, z, heading);
		}
		
		Location newLoc = null;
		
		for (int i = 0; i < 10; i++)
		{
			int radius = Rnd.get(radius_min, radius_max);
			double angle = Rnd.nextDouble() * 2 * Math.PI;
			
			newLoc = new Location((int) (x + (radius * Math.cos(angle))), (int) (y + (radius * Math.sin(angle))), z, heading);
			
			if (GeoZCorrect)
			{
				newLoc.setZ(getInstance().getSpawnHeight(newLoc.getX(), newLoc.getY(), newLoc.getZ()));
			}
			
			if (DoorData.getInstance().checkIfDoorsBetween(actor.getX(), actor.getY(), actor.getZ(), x, y, z, actor.getInstanceId()))
			{
				continue;
			}
			
			if (getInstance().canMoveToCoord(x, y, z, newLoc.getX(), newLoc.getY(), newLoc.getZ(), true))
			{
				break;
			}
		}
		
		return newLoc;
	}
	
	public static Location coordsRandomize(int x, int y, int z, int heading, int radius_min, int radius_max, boolean GeoZCorrect)
	{
		if ((radius_max == 0) || (radius_max < radius_min))
		{
			return new Location(x, y, z, heading);
		}
		
		Location newLoc = null;
		
		for (int i = 0; i < 10; i++)
		{
			int radius = Rnd.get(radius_min, radius_max);
			double angle = Rnd.nextDouble() * 2 * Math.PI;
			
			newLoc = new Location((int) (x + (radius * Math.cos(angle))), (int) (y + (radius * Math.sin(angle))), z, heading);
			
			if (GeoZCorrect)
			{
				newLoc.setZ(getInstance().getSpawnHeight(newLoc.getX(), newLoc.getY(), newLoc.getZ()));
			}
			
			if (getInstance().canMoveToCoord(x, y, z, newLoc.getX(), newLoc.getY(), newLoc.getZ(), true))
			{
				break;
			}
		}
		
		return newLoc;
	}
	
	/*
	 * public Location findPointToStay(int x, int y, int z, int j, int k) { Location pos = new Location(x, y, z); for (int i = 0; i < 100; i++) { pos = coordsRandomize(x, y, z, 0, j, k); if (canMoveToCoord(x, y, z, pos.getX(), pos.getY(), pos.getZ()) && canMoveToCoord(pos.getX(), pos.getY(),
	 * pos.getZ(), x, y, z)) break; } return pos; }
	 */
	
	public int getSpawnHeight(L2Character cha)
	{
		return getSpawnHeight(cha.getX(), cha.getY(), cha.getZ());
	}
	
	public int getSpawnHeight(Location loc)
	{
		return getSpawnHeight(loc.getX(), loc.getY(), loc.getZ());
	}
	
	public int getSpawnHeight(int x, int y, int zmin)
	{
		if (Config.GEODATA)
		{
			return geoEngine.getSpawnHeight(x, y, zmin);
		}
		return zmin;
	}
	
	@SuppressWarnings("unused")
	public void applyGeoCollision(GeoCollision collision)
	{
		if (Config.GEODATA && (1 == 0))
		{
			geoEngine.applyGeoCollision(collision);
		}
	}
	
	@SuppressWarnings("unused")
	public void removeGeoCollision(GeoCollision collision)
	{
		if (Config.GEODATA && (1 == 0))
		{
			geoEngine.removeGeoCollision(collision);
		}
	}
}