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
public class EntryServlet extends NetplayServlet
{
	private static final long serialVersionUID = 1L;
	/*
	private static final byte[] fakeId = new byte[] {
			0, (byte)0xfc, 0x6d, 0x67, 0x6d, 0x7a, 0x64, 0x62, 0x74, 0x75, 0x32, 0, 0, 0, 0, 0,
			0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
			0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
			0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
			0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
			0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0x4e, 0x45, 0x54, 0, 0x46,
			0x4c, 0x59, 0x49, 0x4e, 0x47, 0x48, 0x45, 0x41, 0x44, 0x20, 0x20, 0x20, 0x55, 0x53, 0, 1,
			0, 0, 0, 0, 0, 0, 0, 0xa, 0, 0, 0, 0, 0, 0, 0, 7, 1, 0, 0
	};
	*/

	@Override
	protected void process(byte[] data, HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException
	{
		if (data[0] == 0)
		{
			// Register entry
			int id = makeId();
			byte[] entryData = Arrays.copyOfRange(data, 3, 3 + 128);
			Races.Entry entry = getRaces().addEntry(id, entryData);
			boolean flycast = new String(entryData, 0, 8).equals("gmzdbtu2"); // flycast1 ROT1
			log("New entry " + entry.getName() + " circuit " + F355.getCircuitName(entry.circuit) + (flycast ? " (Flycast)" : " (Real console)"));
			// car # -1
			//int carNum = entryData[0x7c];
			// car color:
			// 0	red
			// 1	yellow
			// 2	grey
			// 3	white
			// 4	green
			// 5	orange
			// 6	red & blue
			// 7	white & green
			//int carColor = entryData[0x7d];
			// should be returning 2 identifiers: private id, public id
			byte[] outdata = new byte[8];
			idToBytes(id, outdata, 0);
			idToBytes(id, outdata, 4);
			respond(outdata, resp);
			/*
			if (getRaces().getWaitingListSize() == 1) {
				if (fakeId.length != 131)
					throw new RuntimeException("fakeId has " + fakeId.length + " bytes");
				for (int i = 0; i < 15; i++)
				{
					byte[] fakeEntryData = Arrays.copyOfRange(fakeId, 3, 3 + 128);
					fakeEntryData[0x7c] = (byte)F355.getRandomInt(100);	// car #
					fakeEntryData[0x7d] = (byte)F355.getRandomInt(8);	// car color
					fakeEntryData[108] = entryData[108];	// circuit
					fakeEntryData[92] = 'F';
					fakeEntryData[93] = 'a';
					fakeEntryData[94] = 'k';
					fakeEntryData[95] = 'e';
					fakeEntryData[96] = ' ';
					if (i < 9) {
						fakeEntryData[97] = (byte)((int)'1' + i);
						fakeEntryData[98] = ' ';
					}
					else {
						fakeEntryData[97] = '1';
						fakeEntryData[98] = (byte)((int)'0' + i - 9);
					}
					fakeEntryData[99] = ' ';
					fakeEntryData[100] = ' ';
					fakeEntryData[101] = ' ';
					fakeEntryData[102] = ' ';
					fakeEntryData[103] = ' ';
					fakeEntryData[105] = 'B';
					fakeEntryData[106] = 'R';
					id = makeId();
					entry = getRaces().addEntry(id, fakeEntryData);
					log("Fake entry " + entry.getName() + " circuit " + F355.getCircuitName(entry.circuit));
				}
			}
			*/
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
					if (entry != null)
						// might have timed out
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
