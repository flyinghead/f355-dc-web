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
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Properties;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;

import org.json.JSONArray;
import org.json.JSONObject;

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
	
	private void updateServerStatus(Races races) throws IOException
	{
		if (lastServerUpdate == 0)
		{
			try {
				Properties props = new Properties();
				props.load(new FileInputStream("/usr/local/etc/dcnet/status.conf"));
				String update = props.getProperty("update-interval", "300");
				updateInterval = Integer.parseInt(update);
			} catch (NumberFormatException e) {
				updateInterval = 300;
			} catch (IOException e) {
				e.printStackTrace();
				updateInterval = 300;
			}
		}
		if (System.currentTimeMillis() - lastServerUpdate < updateInterval * 1000) // default is 5 min
			return;
		lastServerUpdate = System.currentTimeMillis();
		JSONObject status = new JSONObject();
		status.put("gameId", "f355");
		status.put("timestamp", System.currentTimeMillis() / 1000);
		status.put("playerCount", races.getTotalPlayerCount());
		status.put("gameCount", races.getRaceCount());
		JSONArray array = new JSONArray();
		array.put(status);
		String text = array.toString(4);
		OutputStream ostream = new FileOutputStream("/var/lib/dcnet/status/f355");
		try {
			ostream.write(text.getBytes("UTF-8"));
		} finally {
			ostream.close();
		}
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
				updateServerStatus(races);
			} catch (IOException e) {
				context.log("DCNet server status update failed", e);
			}
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
	private long lastServerUpdate = 0L;
	private long updateInterval = 300;
}
