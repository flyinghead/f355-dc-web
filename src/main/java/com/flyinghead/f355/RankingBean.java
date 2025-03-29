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

import java.util.List;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

import org.springframework.context.ApplicationContext;
import org.springframework.web.servlet.support.RequestContextUtils;

import com.flyinghead.f355.db.IDbService;
import com.flyinghead.f355.db.Result;

public class RankingBean
{
	private HttpServletRequest request;
	private String language = "en_US";
	private int circuit = 1;
	private String circuitName;
	private boolean semiAuto;
	private String headgif;
	private String headTgif;
	private int index;
	private int listIndex;
	private int country;
	private int assisted;
	private int tuned;
	private int raceMode;
	private int machine;
	private List<Result> results;
	
	public void setRequest(ServletRequest request)
	{
		this.request = (HttpServletRequest)request;

		String path = this.request.getServletPath();
		if (path.contains("ranking_uk"))
			language = "en_GB";
		else if (path.contains("ranking_fr"))
			language = "fr";
		else if (path.contains("ranking_de"))
			language = "de";
		else if (path.contains("ranking_it"))
			language = "it";
		else if (path.contains("ranking_es"))
			language = "es";
		else if (path.contains("ranking_ja"))
			language = "ja";

		String s = request.getParameter("circuit");
		if (s != null)
			try {
				circuit = Integer.valueOf(s);
			} catch (NumberFormatException e) {}
		circuitName = F355.getCircuitName(circuit);
		
		headgif = "notfound.gif";
		headTgif = "notfound.gif";
		switch (circuit)
		{
		case 0:
			headgif = "F355_ssuz.gif";
			headTgif = "F355_T_ssuz.gif";
			break;
		case 1:
			headgif = "F355_mote.gif";
			headTgif = "F355_T_mote.gif";
			break;
		case 2:
			headgif = "F355_suzu.gif";
			headTgif = "F355_T_suzu.gif";
			break;
		case 3:
			headgif = "F355_long.gif";
			headTgif = "F355_T_long.gif";
			break;
		case 4:
			headgif = "F355_sugo.gif";
			headTgif = "F355_T_sugo.gif";
			break;
		case 5:
			headgif = "F355_monz.gif";
			headTgif = "F355_T_monz.gif";
			break;

		case 7:
			headgif = "F355_fior.gif";
			headTgif = "F355_T_fior.gif";
			break;
		case 8:
			headgif = "F355_nurb.gif";
			headTgif = "F355_T_nurb.gif";
			break;
		case 9:
			headgif = "F355_lagu.gif";
			headTgif = "F355_T_lagu.gif";
			break;
		case 10:
			headgif = "F355_sepa.gif";
			headTgif = "F355_T_sepa.gif";
			break;
		case 11:
			headgif = "F355_atla.gif";
			headTgif = "F355_T_atla.gif";
			break;
		}

		semiAuto = "1".equals(request.getParameter("semiAuto"));
		
		index = 1;
		s = request.getParameter("index");
		if (s != null)
			index = Integer.valueOf(s);
		listIndex = index;
		
		ApplicationContext ac = RequestContextUtils.findWebApplicationContext(this.request);
		IDbService dbService = (IDbService)ac.getBean("dbService");

		country = IDbService.ALL;
		s = request.getParameter("country");
		if (s != null)
			country = Integer.valueOf(s);
		assisted = IDbService.ALL;
		if ("0".equals(request.getParameter("assisted")))
			assisted = 0;
		else if ("1".equals(request.getParameter("assisted")))
			assisted = 1;
		tuned = IDbService.ALL;
		if ("0".equals(request.getParameter("tuned")))
			tuned = 0;
		else if ("1".equals(request.getParameter("tuned")))
			tuned = 1;
		raceMode = IDbService.ALL;
		if ("0".equals(request.getParameter("raceMode")))
			raceMode = 0;
		else if ("1".equals(request.getParameter("raceMode")))
			raceMode = 1;
		machine = IDbService.ALL;
		if ("0".equals(request.getParameter("machine")))
			machine = 0;
		else if ("1".equals(request.getParameter("machine")))
			machine = 1;
		
		results = dbService.getResults(circuit, semiAuto, index - 1, 10, country, assisted, tuned, raceMode, machine);
	}
	
	public void setResponse(ServletResponse resp)
	{
		if (language.equals("ja"))
			resp.setContentType("text/html; charset=x-sjis");
		else
			resp.setContentType("text/html; charset=iso-8859-1");
	}

	public String getCircuitName() {
		return circuitName;
	}

	public String getCircuitNameLow() {
		return circuitName.toLowerCase();
	}

	public String getHeadgif() {
		return headgif;
	}

	public String getHeadTgif() {
		return headTgif;
	}
	
	public String getSemiAutoLabel() {
		return semiAuto ? "SA" : "AT";
	}

	public String getRaceModeIcon(Result result)
	{
		return result.getRaceMode() == 0 ? "icon_training.gif" : result.getRaceMode() == 1 ? "icon_free.gif" : "icon_race.gif";
	}

	public String getRaceModeAlt(Result result)
	{
		return result.getRaceMode() == 0 ? "training" : result.getRaceMode() == 1 ? "drive" : "race";
	}
	
	private String getPageUrl(int index)
	{
		StringBuilder sb = new StringBuilder("view_rank.jsp?");
		sb.append("circuit=").append(circuit)
		  .append("&semiAuto=").append(semiAuto ? "1" : "0")
		  .append("&index=").append(Math.max(1, index));
		if (country != IDbService.ALL)
			sb.append("&country=").append(country);
		if (assisted != IDbService.ALL)
			sb.append("&assisted=").append(assisted);
		if (tuned != IDbService.ALL)
			sb.append("&tuned=").append(tuned);
		if (raceMode != IDbService.ALL)
			sb.append("&raceMode=").append(raceMode);
		if (machine != IDbService.ALL)
			sb.append("&machine=").append(machine);
		
		return sb.toString();
	}

	public String getPreviousPageUrl()
	{
		if (index <= 1)
			return "";
		else
			return getPageUrl(index - 10);
	}

	public String getNextPageUrl()
	{
		if (results.size() < 10)
			return "";
		else
			return getPageUrl(index + 10);
	}
	
	public int getListIndex() {
		return listIndex++;
	}

	public List<Result> getResults() {
		return results;
	}

	public int getTuned() {
		return tuned;
	}

	public void setTuned(int tuned) {
		this.tuned = tuned;
	}

	public int getCircuit() {
		return circuit;
	}

	public int getIndex() {
		return index;
	}

	public int getCountry() {
		return country;
	}

	public int getAssisted() {
		return assisted;
	}

	public int getRaceMode() {
		return raceMode;
	}

	public int getMachine() {
		return machine;
	}
	
	public String getDataUrl(Result result)
	{
		if (result.getDataPath() == null)
			return "";
		String userAgent = request.getHeader("User-Agent");
		if (userAgent == null || (!userAgent.contains("DreamKey") && !userAgent.contains("DreamPassport")))
			return "";
		return "/f355/cgi-bin/f355/download?file=" + result.getDataPath() + "&circuit=" + result.getCircuit();
	}

	public String getLanguage() {
		return language;
	}
	
	public String getF355Home()
	{
		if ("ja".equals(language))
			return "/f355/jp/";
		if ("en".equals(language))
			return "/f355/us/";
		return "/f355/eu/";
	}
}
