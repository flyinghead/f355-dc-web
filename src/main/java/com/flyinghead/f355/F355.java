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

public class F355 {
	private F355() {}
	
	public static final int CIRCUIT_COUNT = 6;

	public static String getCircuitName(int circuit)
	{
		switch (circuit) {
		case 0: return "SUZUKA SHORT";
		case 1: return "MOTEGI";
		case 2: return "SUZUKA";
		case 3: return "LONG BEACH";
		case 4: return "SUGO";
		case 5: return "MONZA";
		default: return "Unknown";
		}
	}
	
	// Max qualifier time in seconds
	public static int getQualifierTime(int circuit)
	{
		switch (circuit) {
		case 0: return 64;
		case 1: return 48;
		case 2: return 154;
		case 3: return 81;
		case 4: return 101;
		case 5: return 129;
		default: return 0;
		}
	}
	
	public static int getLapCount(int circuit)
	{
		switch (circuit) {
		case 0: return 3;
		case 1: return 4;
		case 2: return 2;
		case 3: return 3;
		case 4: return 3;
		case 5: return 2;
		default: return 0;
		}
	}

	public static String getPlayerName(byte[] entry)
	{
		if (entry == null)
			return "Unknown";
		// name (country)
		return new String(entry, 92, 12).trim() 
				+ " (" + new String(entry, 105, 2) + ")";
	}

}
