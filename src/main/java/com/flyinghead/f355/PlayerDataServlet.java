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
import java.util.Base64;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;

import com.flyinghead.f355.db.IDbService;
import com.flyinghead.f355.db.LapTime;
import com.flyinghead.f355.db.Player;

@WebServlet(urlPatterns = {
	"/cgi-bin/f355/dp3_player_data.cgi",
	"/cgi-bin/f355/fr3_player_data.cgi",
	"/cgi-bin/f355/de3_player_data.cgi",
	"/cgi-bin/f355/it3_player_data.cgi",
	"/cgi-bin/f355/es3_player_data.cgi",
})
public class PlayerDataServlet extends AutowiredServlet
{
	private static final long serialVersionUID = 1L;

	@Autowired
    private IDbService dbService;

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException
	{
		String in = req.getReader().readLine();
		QueryParams params = new QueryParams(in);
		String strData = params.get("f355_player_data");
		Player player = null;
		String country = null;
		if (strData != null)
		{
			byte[] bytes = Base64.getDecoder().decode(Base64Scramble.unscramble(strData));
			log("Creating/updating player");
			player = dbService.createUpdatePlayer(bytes, getRemoteIP(req));
			log("Player id is " + player.getId());

			String userAgent = req.getHeader("User-agent");
			if (userAgent != null && userAgent.contains("DreamPassport"))
				country = "ja";
			else
			{
				String referer = req.getHeader("Referer");
				if (referer != null && referer.endsWith("F355DATA.PLY?u"))
					country = "uk";
				else
				{
					country = req.getServletPath();
					if (country.length() < 16)
						country = "en";
					else {
						country = country.substring(14, 16);
						if (country.equals("dp"))
							country = "en";
					}
				}
			}
		}
		else
		{
			int playerId = Integer.valueOf(params.get("playerId"));
			country = params.get("country");
			if (params.get("update") != null) {
				log("Updating score name");
				player = dbService.updateScoreName(playerId, params.get("scoreName"), getRemoteIP(req));
			}
			else if (params.get("registerAll") != null)
			{
				log("Registering all times");
				player = dbService.getPlayer(playerId);
				for (int i = 0; i < F355.CIRCUIT_COUNT; i++)
				{
					LapTime lapTime = player.getLapTime(i, false);
					if (lapTime != null) {
						log("Registering time for track " + F355.getCircuitName(i) + " AT");
						dbService.saveBestLap(playerId, i, false);
					}
					lapTime = player.getLapTime(i, true);
					if (lapTime != null) {
						log("Registering time for track " + F355.getCircuitName(i) + " SA");
						dbService.saveBestLap(playerId, i, true);
					}
				}
			}
			else
			{
				for (int i = 0; i < F355.CIRCUIT_COUNT; i++)
				{
					String v = params.get("atTrack" + i);
					if (v != null)
					{
						log("Registering time for track " + F355.getCircuitName(i) + " AT");
						player = dbService.saveBestLap(playerId, i, false);
						break;
					}
					v = params.get("saTrack" + i);
					if (v != null)
					{
						log("Registering time for track " + F355.getCircuitName(i) + " SA");
						player = dbService.saveBestLap(playerId, i, true);
						break;
					}
				}
			}
		}

		RegisterBean bean = new RegisterBean();
		bean.setPlayer(player);
		bean.setCountry(country);
		req.setAttribute("bean", bean);
		RequestDispatcher reqDispatcher = req.getRequestDispatcher("/ranking_" + country + "/RANK/RANKCOURSE/register_time.jsp");
		reqDispatcher.forward(req, resp);
	}
}
