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

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.servlet.ServletContext;

public class Races
{
	public static class Entry
	{
		private Entry(int id, byte[] entryData)
		{
			this.id = id;
			this.entryData = entryData;
			circuit = Math.min(F355.CIRCUIT_COUNT - 1, Math.max(0, entryData[108]));
			intermediate = entryData[112] != 0;
			weather = entryData[116];
		}
		
		public String getName() {
			return F355.getPlayerName(entryData);
		}

		int id;
		byte[] entryData;
		int circuit;
		boolean intermediate;
		int weather;
		Date lastHeardOf = new Date(); // entry checked every 3 sec
		Date created = new Date();
	}
	
	private static class WaitingList
	{
		void addEntry(int id, Entry entry)
		{
			timeoutEntries();
			entries.put(id, entry);
		}
		
		int checkEntry(int id)
		{
			timeoutEntries();
			Entry entry = entries.get(id);
			if (entry == null)
				return -1;
			entry.lastHeardOf = new Date();
			return entries.size();
		}

		void timeoutEntries()
		{
			long timeout = System.currentTimeMillis() - 20000;	// 20 sec time out
			List<Entry> timedoutEntries = entries.values().stream()
					.filter(e -> e.lastHeardOf.getTime() <= timeout)
					.collect(Collectors.toList());
			for (Entry entry : timedoutEntries)
				entries.remove(entry.id);
		}
		
		private Map<Integer, Entry> entries = new HashMap<>();
	}

	public synchronized Race findRace(int id)
	{
		Race race = raceById.get(id);
		if (race != null)
			return race;
		Entry entry = waitingList.entries.get(id);
		if (entry != null)
			checkStartRace();
		// a race might have started but the user isn't necessarily part of it
		return raceById.get(id);
	}

	private void enterRace(Race race, int id, byte[] entry)
	{
		race.setEntry(id, entry);
		raceById.put(id, race);
	}
	
	public synchronized Entry addEntry(int id, byte[] entryData)
	{
		Entry entry = new Entry(id, entryData);
		waitingList.addEntry(id, entry);
		return entry;
	}
	
	public synchronized int checkEntry(int id)
	{
		return waitingList.checkEntry(id);
	}

	public synchronized Entry getEntry(int id)
	{
		return waitingList.entries.get(id);
	}

	private Race checkStartRace()
	{
		if (waitingList.entries.size() <= 1)
			return null;
		List<Entry> racers = null;
		if (waitingList.entries.size() >= 16)
		{
			// Start the race now with the 16 oldest entries
			racers = waitingList.entries.values().stream()
					.sorted((e1, e2) -> e1.created.compareTo(e2.created))
					.limit(16)
					.collect(Collectors.toList());
		}
		else
		{
			// Start the race if at least 2 entries have been waiting for more that 60 sec
			long timeout = System.currentTimeMillis() - 60000;
			int timeoutEntries = 0;
			for (Entry entry : waitingList.entries.values())
			{
				if (entry.created.getTime() < timeout)
					timeoutEntries++;
			}
			if (timeoutEntries < 2)
				return null;
			racers = new ArrayList<>(waitingList.entries.values());
		}
		int[] votes = new int[F355.CIRCUIT_COUNT];
		for (Entry entry : racers)
			votes[entry.circuit]++;
		int votedCircuit = 0;
		int maxVotes = 0;
		for (int i = 0; i < votes.length; i++)
			if (votes[i] > maxVotes) {
				maxVotes = votes[i];
				votedCircuit = i;
			}

		Race race = new Race(votedCircuit, racers.get(0).weather);
		races.add(race);
		for (Entry entry : racers) {
			enterRace(race, entry.id, entry.entryData);
			waitingList.entries.remove(entry.id);
		}
		race.setStatus(1);
		
		return race;
	}
	
	private byte[] getDefaultResult(ServletContext context, Race race)
	{
		String path = "/WEB-INF/ghost/" + race.getCircuitName() + "_1.bin";
		try {
			InputStream is = context.getResourceAsStream(path);
			if (is == null)
				throw new FileNotFoundException();
			byte[] buf = new byte[65536];
			int l = is.read(buf);
			is.close();
			return Arrays.copyOfRange(buf, 0, l);
		} catch (IOException e) {
			context.log("Can't load default ghost \"" + path + '"', e);
			return null;
		}
	}

	public synchronized void timeoutRaces(ServletContext context)
	{
		List<Race> timeoutRaces = new ArrayList<>();
		for (Race race : races)
		{
			long time = 0;
			if (race.getStatus() == 1)
				// add a 60 sec tolerance to the max qualifier time
				time = (F355.getQualifierTime(race.getCircuit()) + 60) * 1000;
			else if (race.getStatus() == 2)
				// add 2 min to the expected race time
				time = (F355.getQualifierTime(race.getCircuit()) * F355.getLapCount(race.getCircuit()) + 120) * 1000;
			if (time != 0 && race.getStartTime().getTime() + time < System.currentTimeMillis())
				timeoutRaces.add(race);
		}
		for (Race race : timeoutRaces)
		{
			if (race.getStatus() == 1 && race.getEntryCount() >= 3)
			{
				// Timeout individual racer if at least 2 remain
				for (int id : race.getEntryIds())
					if (race.getQualifier(id) == null) {
						context.log("Race " + race.getCircuitName() + " qualifier " + race.getEntryName(id) + " has timed out");
						race.deleteEntry(id);
						raceById.remove(id);
					}
				if (race.getEntryCount() >= 2)
					// Allow the race to start
					continue;
			}
			else if (race.getStatus() == 2)
			{
				// Use default result for timed out drivers
				byte [] defaultResult = null;
				for (int id : race.getEntryIds())
					if (race.getResult(id) == null)
					{
						if (defaultResult == null)
						{
							defaultResult = getDefaultResult(context, race);
							if (defaultResult == null)
								break;
						}
						race.setResult(id, defaultResult);
						context.log("Race " + race.getCircuitName() + " driver " + race.getEntryName(id) + " has timed out");
					}
				if (defaultResult != null)
				{
					race.addTime(60000);
					continue;
				}
			}
			context.log("Race " + race.getCircuitName() + " state " + race.getStatus() + " timed out");
			for (int id : race.getEntryIds())
				raceById.remove(id);
			races.remove(race);
		}
		waitingList.timeoutEntries();
	}
	
	public synchronized int getWaitingListSize() {
		return waitingList.entries.size();
	}
	public synchronized int getRaceCount() {
		return races.size();
	}

	private List<Race> races = new ArrayList<>();
	private Map<Integer, Race> raceById = new HashMap<>(); 
	private WaitingList waitingList = new WaitingList();
}
