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

import com.flyinghead.f355.db.LapTime;
import com.flyinghead.f355.db.Player;

public class RegisterBean
{
	private Player player;
	private String country;
	private String uploadMessage = "";

	public Player getPlayer() {
		return player;
	}

	public void setPlayer(Player player) {
		this.player = player;
	}

	private String getLapTimeString(int t) {
		if (t == -1)
			return "";
		else
			return String.format("%1$d'%2$02d&quot;%3$03d", t / 100000, (t % 100000) / 1000, t % 1000);
	}

	public String getPlayerName() {
		return player.getName();
	}

	public int getRaceCount() {
		return player.getPlayCount();
	}

	public String getDistanceMiles() {
		return String.format("%1$.1f", player.getDistanceDriven() / 1609.344);
	}
	
	public String getDistanceKm() {
		return String.format("%1$.1f", player.getDistanceDriven() / 1000.0);
	}
	
	public String getLapTime(int track, boolean semiAuto) {
		LapTime lapTime = player.getLapTime(track, semiAuto);
		return getLapTimeString(lapTime.getTime());
	}
	
	public boolean hasResult(int track, boolean semiAuto)
	{
		return player.getLapTime(track, semiAuto) != null;
	}
	
	public boolean isAssist(int track, boolean semiAuto)
	{
		return player.getLapTime(track, semiAuto).isAssist();
	}

	public boolean isTuned(int track, boolean semiAuto)
	{
		return player.getLapTime(track, semiAuto).isTuned();
	}

	public String getRaceModeIcon(int track, boolean semiAuto)
	{
		switch (player.getLapTime(track, semiAuto).getRaceMode())
		{
		case 0: return "icon_training.gif";
		case 1: return "icon_free.gif";
		case 2: return "icon_race.gif";
		default: return "icon_dontknow.gif";
		}
	}
	
	public int[] getTracks() {
		return new int[] { 1, 0, 5, 4, 2, 3, 11, 8, 9, 10, 7 };
	}
	
	public String getTrackName(int circuit) {
		return F355.getCircuitName(circuit);
	}

	public String getCountry() {
		return country;
	}

	public void setCountry(String country) {
		this.country = country;
	}

	public String getUploadMessage() {
		return uploadMessage;
	}

	public void setUploadMessage(String uploadMessage) {
		this.uploadMessage = uploadMessage;
	}
}
