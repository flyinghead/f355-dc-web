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

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpUtils;

public class QueryParams
{
	public QueryParams(String q) {
		Map<String, String[]> tmp = HttpUtils.parseQueryString(q);
		params = new HashMap<>(tmp.size());
		for (String name : tmp.keySet())
			params.put(name, tmp.get(name)[0]);
	}
	
	public String get(String name) {
		return params.get(name);
	}
	
	private Map<String, String> params;
}
