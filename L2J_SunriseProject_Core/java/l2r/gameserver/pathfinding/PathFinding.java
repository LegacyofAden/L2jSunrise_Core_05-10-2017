/*
 * Copyright (C) 2004-2015 L2J Server
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
package l2r.gameserver.pathfinding;

import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import l2r.Config;
import l2r.gameserver.idfactory.IdFactory;
import l2r.gameserver.model.L2World;
import l2r.gameserver.model.items.instance.L2ItemInstance;
import l2r.gameserver.pathfinding.cellnodes.CellPathFinding;
import l2r.gameserver.pathfinding.geonodes.GeoPathFinding;

/**
 * @author -Nemesiss-
 */
public abstract class PathFinding
{
	private final static Set<L2ItemInstance> _debugItems = ConcurrentHashMap.newKeySet();
	
	public static PathFinding getInstance()
	{
		if (Config.PATHFINDING == 1)
		{
			// Higher Memory Usage, Smaller Cpu Usage
			return GeoPathFinding.getInstance();
		}
		// Cell pathfinding, calculated directly from geodata files
		return CellPathFinding.getInstance();
	}
	
	public abstract boolean pathNodesExist(short regionoffset);
	
	public abstract List<AbstractNodeLoc> findPath(int x, int y, int z, int tx, int ty, int tz, int instanceId, boolean playable);
	
	/**
	 * Convert geodata position to pathnode position
	 * @param geo_pos
	 * @return pathnode position
	 */
	public short getNodePos(int geo_pos)
	{
		return (short) (geo_pos >> 3); // OK?
	}
	
	/**
	 * Convert node position to pathnode block position
	 * @param node_pos
	 * @return pathnode block position (0...255)
	 */
	public short getNodeBlock(int node_pos)
	{
		return (short) (node_pos % 256);
	}
	
	public byte getRegionX(int node_pos)
	{
		return (byte) ((node_pos >> 8) + L2World.TILE_X_MIN);
	}
	
	public byte getRegionY(int node_pos)
	{
		return (byte) ((node_pos >> 8) + L2World.TILE_Y_MIN);
	}
	
	public short getRegionOffset(byte rx, byte ry)
	{
		return (short) ((rx << 5) + ry);
	}
	
	/**
	 * Convert pathnode x to World x position
	 * @param node_x rx
	 * @return
	 */
	public int calculateWorldX(short node_x)
	{
		return L2World.MAP_MIN_X + (node_x * 128) + 48;
	}
	
	/**
	 * Convert pathnode y to World y position
	 * @param node_y
	 * @return
	 */
	public int calculateWorldY(short node_y)
	{
		return L2World.MAP_MIN_Y + (node_y * 128) + 48;
	}
	
	public String[] getStat()
	{
		return null;
	}
	
	public static void dropDebugItem(int itemId, int num, AbstractNodeLoc loc)
	{
		final L2ItemInstance item = new L2ItemInstance(IdFactory.getInstance().getNextId(), itemId);
		item.setCount(num);
		item.spawnMe(loc.getX(), loc.getY(), loc.getZ());
		_debugItems.add(item);
	}
	
	/**
	 * Clear item drop list for debugging paths.
	 */
	public void clearDebugItems()
	{
		for (L2ItemInstance item : _debugItems)
		{
			item.decayMe();
		}
		
		_debugItems.clear();
	}
}
