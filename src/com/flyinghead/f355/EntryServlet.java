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

@WebServlet("/cgi-bin/f355/network_play/entry.cgi")
public class EntryServlet extends BaseServlet
{
	private static final long serialVersionUID = 1L;

	@Override
	protected void process(byte[] data, HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException
	{
		if (data[0] == 0)
		{
			// Register entry
			int id = makeId();
			byte[] entryData = Arrays.copyOfRange(data, 3, 3 + 128);
			Races.Entry entry = getRaces().addEntry(id, entryData);
			log("New entry " + entry.getName() + " circuit " + F355.getCircuitName(entry.circuit));
			// should be returning 2 identifiers: private id, public id
			byte[] outdata = new byte[8];
			idToBytes(id, outdata, 0);
			idToBytes(id, outdata, 4);
			respond(outdata, resp);
		}
		else
		{
			// Get race status
			int id = bytesToId(data, 3);
			Race race = getRaces().findRace(id);
			if (race != null)
			{
				log("entry[1] race started: " + race.getCircuitName() + " for racer " + race.getEntryName(id));
				byte[] outdata = {
					1, 0, 0, 0,								// status: 0:waiting, 1:game start
					(byte)race.getEntryCount(), 0, 0, 0,	// # entries
					(byte)race.getCircuit(), 0, 0, 0,
					(byte)race.getWeather(), 0, 0, 0
				};
				respond(outdata, resp);
			}
			else
			{
				int entries = getRaces().checkEntry(id);
				if (entries == -1) {
					log("entry[1] not found: " + Integer.toHexString(id));
					respondError(1, resp);
				}
				else
				{
					Races.Entry entry = getRaces().getEntry(id);
					log("entry[1]: " + entry.getName() + " waiting...");
					byte[] outdata = {
						0, 0, 0, 0, 			// status: 0:waiting, 1:game start
						(byte)entries, 0, 0, 0, // # entries
						0, 0, 0, 0,
						0, 0, 0, 0
					};
					respond(outdata, resp);
				}
			}
		}
		resp.setStatus(HttpServletResponse.SC_OK);
	}
}
