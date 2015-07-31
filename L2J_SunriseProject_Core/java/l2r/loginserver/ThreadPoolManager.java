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
package l2r.loginserver;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import com.l2jserver.mmocore.threading.LoggingRejectedExecutionHandler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author vGodFather
 */
public class ThreadPoolManager
{
	protected static final Logger _log = LoggerFactory.getLogger(ThreadPoolManager.class);
	
	private final ThreadPoolExecutor _executor;
	
	public static ThreadPoolManager getInstance()
	{
		return SingletonHolder._instance;
	}
	
	protected ThreadPoolManager()
	{
		_executor = new ThreadPoolExecutor(8, Integer.MAX_VALUE, 5L, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>(), new PriorityThreadFactory("ThreadPoolExecutor", Thread.NORM_PRIORITY), new LoggingRejectedExecutionHandler());
	}
	
	private static class PriorityThreadFactory implements ThreadFactory
	{
		private final int _prio;
		private final String _name;
		private final AtomicInteger _threadNumber = new AtomicInteger(1);
		private final ThreadGroup _group;
		
		public PriorityThreadFactory(String name, int prio)
		{
			_prio = prio;
			_name = name;
			_group = new ThreadGroup(_name);
		}
		
		@Override
		public Thread newThread(Runnable r)
		{
			Thread t = new Thread(_group, r, _name + "-" + _threadNumber.getAndIncrement());
			t.setPriority(_prio);
			return t;
		}
	}
	
	public void execute(Runnable r)
	{
		_executor.execute(wrap(r));
	}
	
	public Runnable wrap(Runnable r)
	{
		return r;
	}
	
	private static class SingletonHolder
	{
		protected static final ThreadPoolManager _instance = new ThreadPoolManager();
	}
}