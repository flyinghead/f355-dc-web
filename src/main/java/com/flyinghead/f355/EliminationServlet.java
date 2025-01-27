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
public class EliminationServlet extends NetplayServlet
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
				// Don't report error just yet
				respond(new byte[0], resp);
				return;
			}
			if (race.getStatus() != Race.STATUS_QUALIF)
			{
				log("elimination[0] Race " + race.getCircuitName() + " already started (for " + race.getEntryName(id) + ")");
				respondError(1, resp);
				return;
			}
			byte[] qualifier = Arrays.copyOfRange(data, 11, 11 + 8);
			int frames = bytesToId(qualifier, 0);
			int frac = bytesToId(qualifier, 4);
			String timeStr;
			if (frames == 0xfffff) {
				timeStr = "No Goal (" + Float.intBitsToFloat(frac) + ")";
			}
			else
			{
				float time = ((float)frames + Float.intBitsToFloat(frac)) / 60.2f;
				int min = (int)time / 60;
				time -= (float)min * 60.f;
				int sec = (int)time;
				time -= (float)sec;
				int msec = (int)(time * 1000.f);
				timeStr = String.format("%1$02d'%2$02d\"%3$03d", min, sec, msec);
			}
			log("Race " + race.getCircuitName() + " qualifier received for " + race.getEntryName(id) + ": " + timeStr);
			race.setQualifier(id, qualifier);

			// Nothing to return
			respond(new byte[0], resp);
			/*
			for (int fid : race.getEntryIds()) {
				if (race.getEntryName(fid).startsWith("Fake "))
				{
					byte[] fakeQualif = new byte[8];
					fakeQualif[0] = (byte)0xff;
					fakeQualif[1] = (byte)0xff;
					fakeQualif[2] = 0xf;
					fakeQualif[3] = 0;
					int i = Float.floatToIntBits(fid / -9.993470179f);
					idToBytes(i, fakeQualif, 4);
					race.setQualifier(fid, fakeQualif);
				}
			}
			*/
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
					// Race cancelled: all other drivers retired
					byte[] outdata = new byte[18 * 4];
					outdata[0] = 1;
					respond(outdata, resp);
					return;
				}
				boolean qualifDone = race.isQualifierDone();
				if (qualifDone) {
					race.setStatus(Race.STATUS_FINAL);
					//log("Qualifier ranking:");
					//for (Integer rid : race.getEntryIds())
					//	log(race.getEntryName(rid) + ": " + race.getQualifierRanking(rid));
				}
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
