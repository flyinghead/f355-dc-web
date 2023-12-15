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

import java.sql.Timestamp;
import java.util.Calendar;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Entity
@Table(name = "\"result\"")
public class Result
{
	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
	private int id;

	@ManyToOne
	@JoinColumn(name="player_id")
	private Player player;

	@Column(name = "circuit")
	private int circuit;

	@Column(name = "semi_auto")
	private boolean semiAuto;
	
	@Column(name = "run_date")
	private Timestamp runDate;
	
	@Column(name = "tuned")
	private boolean tuned;
	
	@Column(name = "assisted")
	private boolean assisted;
	
	@Column(name = "race_mode")
	private int raceMode;
	
	@Column(name = "arcade")
	private boolean arcade;
	
	@Column(name = "data_path")
	private String dataPath;

	@Column(name = "run_time")
	private int runTime;

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public Player getPlayer() {
		return player;
	}

	public void setPlayer(Player player) {
		this.player = player;
	}

	public int getCircuit() {
		return circuit;
	}

	public void setCircuit(int circuit) {
		this.circuit = circuit;
	}

	public boolean isSemiAuto() {
		return semiAuto;
	}

	public void setSemiAuto(boolean semiAuto) {
		this.semiAuto = semiAuto;
	}

	public Timestamp getRunDate() {
		return runDate;
	}
	public String getRunDateString() {
		Calendar cal = Calendar.getInstance();
		cal.setTime(runDate);
		return String.format("%1$04d/%2$02d/%3$02d", cal.get(Calendar.YEAR), cal.get(Calendar.MONTH) + 1, cal.get(Calendar.DAY_OF_MONTH));
	}

	public void setRunDate(Timestamp runDate) {
		this.runDate = runDate;
	}

	public boolean isTuned() {
		return tuned;
	}

	public void setTuned(boolean tuned) {
		this.tuned = tuned;
	}

	public boolean isAssisted() {
		return assisted;
	}

	public void setAssisted(boolean assisted) {
		this.assisted = assisted;
	}

	public int getRaceMode() {
		return raceMode;
	}

	public void setRaceMode(int raceMode) {
		this.raceMode = raceMode;
	}

	public boolean isArcade() {
		return arcade;
	}

	public void setArcade(boolean arcade) {
		this.arcade = arcade;
	}

	public String getDataPath() {
		return dataPath;
	}

	public void setDataPath(String dataPath) {
		this.dataPath = dataPath;
	}

	public int getRunTime() {
		return runTime;
	}
	
	public String getRunTimeString() {
		return String.format("%1$d'%2$02d\"%3$03d", runTime / 100000, (runTime % 100000) / 1000, runTime % 1000);
	}

	public void setRunTime(int runTime) {
		this.runTime = runTime;
	}
}
