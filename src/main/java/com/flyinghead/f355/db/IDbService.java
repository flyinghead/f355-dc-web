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

import java.util.List;

public interface IDbService
{
	// Result selection
	public static final int ALL = -1;
	// country
	public static final int JAPAN = 0;
	public static final int AMERICA = 1;
	public static final int EUROPE = 2;
	public static final int NOT_SET = 3;
	// race mode
	public static final int TRAINING_DRIVING = 0;
	public static final int RACE = 1;
	
	Player getPlayer(int playerId);
	Player createUpdatePlayer(byte[] data, String ip);
	Player updateScoreName(int playerId, String name, String ip);
	Player saveBestLap(int playerId, int circuit, boolean semiAuto);
	Player saveResult(int playerId, byte[] data, String file, String ip);
	List<Result> getResults(int circuit, boolean semiAuto, int index, int count, int country, int assisted, int tuned, int raceMode, int arcade);
}
