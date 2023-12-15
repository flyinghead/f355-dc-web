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
import java.util.Arrays;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet("/cgi-bin/f355/network_play/final.cgi")
public class FinalServlet extends NetplayServlet
{
	private static final long serialVersionUID = 1L;

	@Override
	protected void process(byte[] data, HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException
	{
		if (data[0] == 0)
		{
			// Send race results
			int id = bytesToId(data, 3);
			Race race = getRaces().findRace(id);
			if (race == null)
			{
				log("final[0] No race found for " + Integer.toHexString(id));
				// Don't report error just yet
			}
			else
			{
				if (race.getStatus() != Race.STATUS_FINAL)
				{
					log("final[0] Race " + race.getCircuitName() + " already finished (for " + race.getEntryName(id) + ")");
					respondError(1, resp);
					return;
				}
				log("Race " + race.getCircuitName() + " result received for " + race.getEntryName(id));
				byte[] result = Arrays.copyOfRange(data, 11, data.length);
				race.setResult(id, result);
				if (race.isRaceDone())
					race.setStatus(Race.STATUS_FINISHED);
			}
			// no output
			respond(new byte[0], resp);
		}
		else if (data[0] == 1)
		{
			int id = bytesToId(data, 7);
			if (id != 0)
			{
				// Fetch race results of opponent
				Race race = getRaces().findRace(id);
				if (race == null)
				{
					log("final[1, opponent] No race found for " + Integer.toHexString(id));
					respondError(1, resp);
					return;
				}
				byte[] result = race.getResult(id);
				if (result == null)
				{
					log("final[1, opponent] No result found for " + Integer.toHexString(id));
					respondError(1, resp);
					return;
				}
				respond(result, resp);
				log("Race " + race.getCircuitName() + ": " + race.getEntryName(id) + " result sent to " + race.getEntryName(bytesToId(data, 3)));
			}
			else
			{
				// Get race results status
				id = bytesToId(data, 3);
				Race race = getRaces().findRace(id);
				if (race == null)
				{
					log("final[1, 0] No race found for " + Integer.toHexString(id));
					// Race cancelled: all other drivers retired
					byte[] outdata = new byte[9 * 4];
					outdata[0] = 1;
					respond(outdata, resp);
					return;
				}
				byte[] outdata = {
					(byte)(race.isRaceDone() ? 1 : 0), 0, 0, 0,
					
					0, 0, 0, 0,
					0, 0, 0, 0,
					0, 0, 0, 0,
					0, 0, 0, 0,
					0, 0, 0, 0,
					0, 0, 0, 0,
					0, 0, 0, 0,
					0, 0, 0, 0,
				};
				System.arraycopy(data, 3, outdata, 4, 4);
				int offset = 8;
				for (int rid : race.getEntryIds())
				{
					if (rid != id)
					{
						byte[] result = race.getResult(rid);
						if (result != null) {
							idToBytes(rid, outdata, offset);
							offset += 4;
						}
					}
				}
				respond(outdata, resp);
				log("Race " + race.getCircuitName() + " final queried by " + race.getEntryName(id) + ": status " + outdata[0]);
			}
		}
		else
		{
			resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			return;
		}
		resp.setStatus(HttpServletResponse.SC_OK);
	}	
}
