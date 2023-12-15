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

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;

@WebFilter("/*")
public class ImageFilter implements Filter
{
	@Override
	public void init(FilterConfig config) throws ServletException {
	}

	@Override
	public void destroy() {
	}

	@Override
	public void doFilter(ServletRequest req, ServletResponse resp, FilterChain chain)
			throws IOException, ServletException
	{
		if (req instanceof HttpServletRequest)
		{
			HttpServletRequest request = (HttpServletRequest)req;
			String servletPath = request.getServletPath();
			if (servletPath.startsWith("/ranking_")
					&& (servletPath.endsWith(".gif") || servletPath.endsWith(".jpg") || servletPath.endsWith(".jpeg")))
			{
				// all images are in /ranking_ja
				final String path = "/ranking_ja" + servletPath.substring(11);
				req = new HttpServletRequestWrapper(request) {
					@Override
					public String getServletPath() {
						return path;
					}
				};
			}
		}
		chain.doFilter(req, resp);
	}
}
