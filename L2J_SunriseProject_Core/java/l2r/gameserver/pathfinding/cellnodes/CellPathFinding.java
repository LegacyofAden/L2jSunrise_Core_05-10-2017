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
package l2r.gameserver.pathfinding.cellnodes;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

import l2r.Config;
import l2r.gameserver.GeoData;
import l2r.gameserver.model.itemcontainer.Inventory;
import l2r.gameserver.pathfinding.AbstractNode;
import l2r.gameserver.pathfinding.AbstractNodeLoc;
import l2r.gameserver.pathfinding.PathFinding;
import l2r.util.StringUtil;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Sami, DS Credits to Diamond
 */
public class CellPathFinding extends PathFinding
{
	private static final Logger _log = LoggerFactory.getLogger(CellPathFinding.class);
	private BufferInfo[] _allBuffers;
	private int _findSuccess = 0;
	private int _findFails = 0;
	private int _postFilterUses = 0;
	private int _postFilterPlayableUses = 0;
	private final int _postFilterPasses = 0;
	private long _postFilterElapsed = 0;
	
	public static CellPathFinding getInstance()
	{
		return SingletonHolder._instance;
	}
	
	protected CellPathFinding()
	{
		try
		{
			String[] array = Config.PATHFIND_BUFFERS.split(";");
			
			_allBuffers = new BufferInfo[array.length];
			
			String buf;
			String[] args;
			for (int i = 0; i < array.length; i++)
			{
				buf = array[i];
				args = buf.split("x");
				if (args.length != 2)
				{
					throw new Exception("Invalid buffer definition: " + buf);
				}
				
				_allBuffers[i] = new BufferInfo(Integer.parseInt(args[0]), Integer.parseInt(args[1]));
			}
		}
		catch (Exception e)
		{
			_log.warn(getClass() + ": Problem during buffer init: " + e.getMessage(), e);
			throw new Error("CellPathFinding: load aborted");
		}
	}
	
	@Override
	public boolean pathNodesExist(short regionoffset)
	{
		return false;
	}
	
	@Override
	public List<AbstractNodeLoc> findPath(int x, int y, int z, int tx, int ty, int tz, int instanceId, boolean playable)
	{
		int gx = GeoData.getInstance().getGeoX(x);
		int gy = GeoData.getInstance().getGeoY(y);
		if (!GeoData.getInstance().hasGeo(x, y))
		{
			return null;
		}
		int gz = GeoData.getInstance().getHeight(x, y, z);
		int gtx = GeoData.getInstance().getGeoX(tx);
		int gty = GeoData.getInstance().getGeoY(ty);
		if (!GeoData.getInstance().hasGeo(tx, ty))
		{
			return null;
		}
		int gtz = GeoData.getInstance().getHeight(tx, ty, tz);
		CellNodeBuffer buffer = alloc(64 + (2 * Math.max(Math.abs(gx - gtx), Math.abs(gy - gty))), playable);
		if (buffer == null)
		{
			return null;
		}
		
		boolean debug = playable && Config.DEBUG_PATH;
		if (debug)
		{
			clearDebugItems();
		}
		
		List<AbstractNodeLoc> path = null;
		try
		{
			CellNode result = buffer.findPath(gx, gy, gz, gtx, gty, gtz);
			
			if (debug)
			{
				for (CellNode n : buffer.debugPath())
				{
					if (n.getCost() < 0)
					{
						dropDebugItem(1831, (int) (-n.getCost() * 10), n.getLoc());
					}
					else
					{
						// known nodes
						dropDebugItem(Inventory.ADENA_ID, (int) (n.getCost() * 10), n.getLoc());
					}
				}
			}
			
			if (result == null)
			{
				_findFails++;
				return null;
			}
			
			path = constructPath(result);
		}
		catch (Exception e)
		{
			_log.warn("", e);
			return null;
		}
		finally
		{
			buffer.free();
		}
		
		if ((path.size() < 3) || (Config.MAX_POSTFILTER_PASSES <= 0))
		{
			_findSuccess++;
			return path;
		}
		
		long timeStamp = System.currentTimeMillis();
		_postFilterUses++;
		if (playable)
		{
			_postFilterPlayableUses++;
		}
		
		// get path list iterator
		ListIterator<AbstractNodeLoc> point = path.listIterator();
		
		// get node A (origin)
		int nodeAx = gx;
		int nodeAy = gy;
		int nodeAz = gz;
		
		// get node B
		AbstractNodeLoc nodeB = point.next();
		
		// iterate thought the path to optimize it
		while (point.hasNext())
		{
			// get node C
			AbstractNodeLoc nodeC = path.get(point.nextIndex());
			
			// check movement from node A to node C
			if (GeoData.getInstance().canMove(nodeAx, nodeAy, nodeAz, nodeC.getNodeX(), nodeC.getNodeY(), nodeC.getZ(), instanceId))
			{
				if ((nodeAx == nodeC.getNodeX()) && (nodeAy == nodeC.getNodeY()))
				{
					// can move from node A to node C
					
					// remove node B
					point.remove();
					
					// show skipped nodes
					if (debug)
					{
						PathFinding.dropDebugItem(735, 1, nodeB); // green potion
					}
				}
				else
				{
					// can not move from node A to node C
					
					// set node A (node B is part of path, update A coordinates)
					nodeAx = nodeB.getX();
					nodeAy = nodeB.getY();
					nodeAz = nodeB.getZ();
				}
			}
			
			// set node B
			nodeB = point.next();
		}
		
		if (debug)
		{
			path.forEach(n -> dropDebugItem(65, 1, n));
		}
		
		_postFilterElapsed += System.currentTimeMillis() - timeStamp;
		
		return path;
	}
	
	private List<AbstractNodeLoc> constructPath(AbstractNode<NodeLoc> node)
	{
		// create empty list
		LinkedList<AbstractNodeLoc> list = new LinkedList<>();
		
		// set direction X/Y
		int dx = 0;
		int dy = 0;
		
		// get target parent
		AbstractNode<NodeLoc> parent = node.getParent();
		
		// while parent exists
		while (parent != null)
		{
			// get parent <> target direction X/Y
			final int nx = parent.getLoc().getNodeX() - node.getLoc().getNodeX();
			final int ny = parent.getLoc().getNodeY() - node.getLoc().getNodeY();
			
			// direction has changed?
			if ((dx != nx) || (dy != ny))
			{
				// add node to the beginning of the list
				list.addFirst(node.getLoc());
				
				// update direction X/Y
				dx = nx;
				dy = ny;
			}
			
			// move to next node, set target and get its parent
			node = parent;
			parent = node.getParent();
		}
		return list;
	}
	
	private final CellNodeBuffer alloc(int size, boolean playable)
	{
		CellNodeBuffer current = null;
		for (BufferInfo holder : _allBuffers)
		{
			// Find proper size of buffer
			if (holder._size < size)
			{
				continue;
			}
			
			// Find unlocked NodeBuffer
			for (CellNodeBuffer buffer : holder._buffer)
			{
				if (!buffer.isLocked())
				{
					continue;
				}
				
				holder._uses++;
				if (playable)
				{
					holder._playableUses++;
				}
				
				holder._elapsed += buffer.getElapsedTime();
				return buffer;
			}
			
			// NodeBuffer not found, allocate temporary buffer
			current = new CellNodeBuffer(holder._size);
			current.isLocked();
			
			holder._overflows++;
			if (playable)
			{
				holder._playableOverflows++;
			}
		}
		
		return current;
	}
	
	/**
	 * NodeBuffer container with specified size and count of separate buffers.
	 */
	private static final class BufferInfo
	{
		final int _size;
		final int _count;
		ArrayList<CellNodeBuffer> _buffer;
		
		// statistics
		int _playableUses = 0;
		int _uses = 0;
		int _playableOverflows = 0;
		int _overflows = 0;
		long _elapsed = 0;
		
		public BufferInfo(int size, int count)
		{
			_size = size;
			_count = count;
			_buffer = new ArrayList<>(count);
			
			for (int i = 0; i < count; i++)
			{
				_buffer.add(new CellNodeBuffer(size));
			}
		}
		
		@Override
		public String toString()
		{
			final StringBuilder stat = new StringBuilder(100);
			StringUtil.append(stat, String.valueOf(_size), "x", String.valueOf(_size), " num:", String.valueOf(_buffer.size()), "/", String.valueOf(_count), " uses:", String.valueOf(_uses), "/", String.valueOf(_playableUses));
			if (_uses > 0)
			{
				StringUtil.append(stat, " total/avg(ms):", String.valueOf(_elapsed), "/", String.format("%1.2f", (double) _elapsed / _uses));
			}
			
			StringUtil.append(stat, " ovf:", String.valueOf(_overflows), "/", String.valueOf(_playableOverflows));
			
			return stat.toString();
		}
	}
	
	@Override
	public String[] getStat()
	{
		final String[] result = new String[_allBuffers.length + 1];
		for (int i = 0; i < _allBuffers.length; i++)
		{
			result[i] = _allBuffers[i].toString();
		}
		
		final StringBuilder stat = new StringBuilder(100);
		StringUtil.append(stat, "LOS postfilter uses:", String.valueOf(_postFilterUses), "/", String.valueOf(_postFilterPlayableUses));
		if (_postFilterUses > 0)
		{
			StringUtil.append(stat, " total/avg(ms):", String.valueOf(_postFilterElapsed), "/", String.format("%1.2f", (double) _postFilterElapsed / _postFilterUses), " passes total/avg:", String.valueOf(_postFilterPasses), "/", String.format("%1.1f", (double) _postFilterPasses / _postFilterUses), Config.EOL);
		}
		StringUtil.append(stat, "Pathfind success/fail:", String.valueOf(_findSuccess), "/", String.valueOf(_findFails));
		result[result.length - 1] = stat.toString();
		
		return result;
	}
	
	private static class SingletonHolder
	{
		protected static final CellPathFinding _instance = new CellPathFinding();
	}
}
