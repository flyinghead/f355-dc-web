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

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class Race
{
	static final int STATUS_INIT = 0;
	static final int STATUS_QUALIF = 1;
	static final int STATUS_FINAL = 2;
	static final int STATUS_FINISHED = 3;

	public Race(int circuit, int weather)
	{
		this.circuit = circuit;
		this.weather = weather;
	}

	public int getStatus() {
		return status;
	}
	public synchronized void setStatus(int status) {
		if (status != this.status) {
			this.status = status;
			this.startTime = new Date();
			if (status == STATUS_FINAL)
			{
				// Calculate the qualifier ranking
				List<Integer> ids = new ArrayList<>(getEntryIds());
				ids.sort(new Comparator<Integer>() {
					@Override
					public int compare(Integer i1, Integer i2) {
						byte[] q1 = getQualifier(i1);
						byte[] q2 = getQualifier(i2);
						int frames1 = bytesToId(q1, 0);
						int frames2 = bytesToId(q2, 0);
						if (frames1 != frames2)
							return frames1 - frames2;
						float frac1 = Float.intBitsToFloat(bytesToId(q1, 4));
						float frac2 = Float.intBitsToFloat(bytesToId(q2, 4));
	
						return frac1 < frac2 ? -1 : frac1 > frac2 ? 1 : 0;
					}
				});
				for (int i = 0; i < ids.size(); i++)
					qualifiedRank.put(ids.get(i), i + 1);
			}
		}
	}
	
	private int bytesToId(byte[] array, int offset)
	{
		return (array[offset] & 0xff)
				| ((array[offset + 1] & 0xff) << 8)
				| ((array[offset + 2] & 0xff) << 16)
				| ((array[offset + 3] & 0xff) << 24);
	}

	public int getCircuit() {
		return circuit;
	}
	public String getCircuitName() {
		return F355.getCircuitName(circuit);
	}

	public int getWeather() {
		return weather;
	}
	public Date getStartTime() {
		return startTime;
	}
	public void addTime(int ms) {
		startTime = new Date(startTime.getTime() + ms);
	}

	public synchronized byte[] getEntry(int id) {
		return entries.get(id);
	}
	public synchronized void setEntry(int id, byte[] entry) {
		entries.put(id, entry);
	}
	public synchronized int getEntryCount() {
		return entries.size();
	}
	public synchronized Set<Integer> getEntryIds() {
		return new HashSet<>(entries.keySet());
	}
	public String getEntryName(int id) {
		return F355.getPlayerName(getEntry(id));
	}
	public synchronized void deleteEntry(int id) {
		entries.remove(id);
		qualifiers.remove(id);
		results.remove(id);
	}

	public synchronized byte[] getQualifier(int id) {
		return qualifiers.get(id);
	}
	public synchronized void setQualifier(int id, byte[] result) {
		qualifiers.put(id, result);
	}
	public synchronized boolean isQualifierDone() {
		return entries.size() == qualifiers.size();
	}
	public synchronized boolean hasQualified(int id) {
		Integer rank = qualifiedRank.get(id);
		return rank != null && rank <= 8;
	}
	public synchronized int getQualifierRanking(int id) {
		Integer rank = qualifiedRank.get(id);
		if (rank == null)
			return 17;
		else
			return rank;
	}

	public synchronized byte[] getResult(int id) {
		return results.get(id);
	}
	public synchronized void setResult(int id, byte[] result) {
		results.put(id, result);
	}
	public synchronized boolean isRaceDone(){
		return entries.size() == results.size() || results.size() == 8;
	}

	private int status = STATUS_INIT;
	private int circuit;
	private int weather; // ??
	private Map<Integer, byte[]> entries = new HashMap<>();
	private Map<Integer, byte[]> qualifiers = new HashMap<>();
	private Map<Integer, byte[]> results = new HashMap<>();
	private Map<Integer, Integer> qualifiedRank = new HashMap<>();
	private Date startTime = new Date();
}
