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

	@Override
	public void run()
	{
		context.log("TimeoutTask starting");
		Races races = (Races)context.getAttribute("races");
		while (!stopping)
		{
			races.timeoutRaces(context);
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
