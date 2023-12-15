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
package com.flyinghead.f355.db;

public class LapTime
{
	private int circuit;
	private boolean semiAuto;
	private int time;
	private int raceMode;
	private boolean assist;
	private boolean tuned;
	private boolean arcade;
	
	LapTime(int circuit, boolean semiAuto, int time, int raceMode, boolean assist, boolean tuned, boolean arcade)
	{
		this.circuit = circuit;
		this.semiAuto = semiAuto;
		this.time = time;
		this.raceMode = raceMode;
		this.assist = assist;
		this.tuned = tuned;
		this.arcade = arcade;
	}

	public int getCircuit() {
		return circuit;
	}

	public boolean isSemiAuto() {
		return semiAuto;
	}

	public int getTime() {
		return time;
	}

	public int getRaceMode() {
		return raceMode;
	}

	public boolean isAssist() {
		return assist;
	}

	public boolean isTuned() {
		return tuned;
	}

	public boolean isArcade() {
		return arcade;
	}
}
