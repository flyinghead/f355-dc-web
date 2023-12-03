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

@WebServlet("/cgi-bin/f355/network_play/elimination.cgi")
public class EliminationServlet extends BaseServlet
{
	private static final long serialVersionUID = 1L;

	@Override
	protected void process(byte[] data, HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException
	{
		if (data[0] == 0)
		{
			// Record qualifier time
			int id = bytesToId(data, 3);
			Race race = getRaces().findRace(id);
			if (race == null)
			{
				log("elimination[0] No race found for " + Integer.toHexString(id));
				respondError(1, resp);
				return;
			}
			byte[] qualifier = Arrays.copyOfRange(data, 11, 11 + 8);
			race.setQualifier(id, qualifier);
			log("Race " + race.getCircuitName() + " qualifier received for " + race.getEntryName(id));

			// Nothing to return
			respond(new byte[0], resp);
		}
		else
		{
			int id = bytesToId(data, 7);
			if (id != 0)
			{
				// Fetch opponent qualifier time
				Race race = getRaces().findRace(id);
				if (race == null)
				{
					log("elimination[1, opponent] No race found for " + Integer.toHexString(id));
					respondError(1, resp);
					return;
				}
				byte[] entry = race.getEntry(id);
				byte[] qualifier = race.getQualifier(id);
				if (entry == null || qualifier == null)
				{
					log("elimination[1, opponent] Entry/qualifier not found for " + Integer.toHexString(id));
					respondError(1, resp);
					return;
				}
				byte[] outdata = new byte[128 + 8];
				System.arraycopy(entry, 0, outdata, 0, entry.length);
				System.arraycopy(qualifier, 0, outdata, 128, qualifier.length);
				respond(outdata, resp);
				log("Race " + race.getCircuitName() + ": " + race.getEntryName(id) + " qualifier sent to " + race.getEntryName(bytesToId(data, 3)));
			}
			else
			{
				// Get qualifier result status
				id = bytesToId(data, 3);
				Race race = getRaces().findRace(id);
				if (race == null)
				{
					log("elimination[1, 0] No race found for " + Integer.toHexString(id));
					respondError(1, resp);
					return;
				}
				boolean qualifDone = race.isQualifierDone();
				if (qualifDone)
					race.setStatus(2);
				byte[] outdata = {
					(byte)(qualifDone ? 1 : 0), 0, 0, 0,	// 0:running, 1:all racers done
					1, 0, 0, 0,		// position?
					
					0, 0, 0, 0,
					0, 0, 0, 0,
					0, 0, 0, 0,
					0, 0, 0, 0,
					0, 0, 0, 0,
					0, 0, 0, 0,
					0, 0, 0, 0,
					0, 0, 0, 0,
					0, 0, 0, 0,
					0, 0, 0, 0,
					0, 0, 0, 0,
					0, 0, 0, 0,
					0, 0, 0, 0,
					0, 0, 0, 0,
					0, 0, 0, 0,
					0, 0, 0, 0,
				};
				System.arraycopy(data, 3, outdata, 8, 4);
				int offset = 12;
				for (int rid : race.getEntryIds())
				{
					if (rid != id)
					{
						byte[] qualifier = race.getQualifier(rid);
						if (qualifier != null) {
							idToBytes(rid, outdata, offset);
							offset += 4;
						}
					}
				}
				respond(outdata, resp);
				log("Race " + race.getCircuitName() + " queried by " + race.getEntryName(id) + ": status " + outdata[0]);
			}
		}
		resp.setStatus(HttpServletResponse.SC_OK);
	}	
}
