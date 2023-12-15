/*
	F355 Challenge web server revival
	Copyright (C) 2023 flyinghead

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package com.flyinghead.f355;

import java.io.File;
import java.io.FilenameFilter;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;

@WebListener
public class TimeoutTask implements ServletContextListener, Runnable
{
	@Override
	public void contextDestroyed(ServletContextEvent sce)
	{
		stopping = true;
		if (thread != null)
		{
			thread.interrupt();
			try {
				thread.join();
			} catch (InterruptedException e) {
				context.log("thread.join failed", e);
			}
			thread = null;
		}
		context.removeAttribute("races");
		context = null;
	}

	@Override
	public void contextInitialized(ServletContextEvent sce)
	{
		context = sce.getServletContext();
		context.setAttribute("races", new Races());
		stopping = false;
		thread = new Thread(this);
		thread.start();
	}

	private void cleanUpTempDir()
	{
		File dir = new File(System.getProperty("java.io.tmpdir"));
		File[] files = dir.listFiles(new FilenameFilter() {
			@Override
			public boolean accept(File dir, String name) {
				return name.endsWith(".VMS") || name.endsWith(".VMI");
			}
		});
		long cutoffDate = System.currentTimeMillis() - 60 * 60 * 1000; // 1 hour max
		for (File f : files)
			if (f.lastModified() < cutoffDate)
				f.delete();
	}
	
	@Override
	public void run()
	{
		context.log("TimeoutTask starting");
		Races races = (Races)context.getAttribute("races");
		while (!stopping)
		{
			races.timeoutRaces(context);
			cleanUpTempDir();
			try {
				Thread.sleep(15000);
			} catch (InterruptedException e) {
			}
		}
		context.log("TimeoutTask stopping");
	}

	private Thread thread = null;
	private ServletContext context = null;
	private boolean stopping = false;
}
