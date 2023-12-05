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

import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class Race
{
	public Race(int circuit, int weather)
	{
		this.circuit = circuit;
		this.weather = weather;
	}

	public int getStatus() {
		return status;
	}
	public void setStatus(int status) {
		if (status != this.status) {
			this.status = status;
			this.startTime = new Date();
		}
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

	public synchronized byte[] getResult(int id) {
		return results.get(id);
	}
	public synchronized void setResult(int id, byte[] result) {
		results.put(id, result);
	}
	public synchronized boolean isRaceDone(){
		return entries.size() == results.size();
	}

	private int status = 0; // 0 waiting, 1 elimination, 2 final
	private int circuit;
	private int weather; // ??
	private Map<Integer, byte[]> entries = new HashMap<>();
	private Map<Integer, byte[]> qualifiers = new HashMap<>();
	private Map<Integer, byte[]> results = new HashMap<>();
	private Date startTime = new Date();
}
