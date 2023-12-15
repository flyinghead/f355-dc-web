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

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "\"player\"")
public class Player
{
	private static final int[] TrackOffset = new int[]  {
		1, 0, 4, 5, 3, 2, -1, 11, 8, 9, 10, 7	
	};

	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
	private int id;
	
	@Column(name = "reg_id")
	private String regId;
	
	@Column(name = "name", length = 12)
	private String name;
	
	@Column(name = "score_name", length = 3)
	private String scoreName;
	
	@Column(name = "country", length = 3)
	private String country;

	@Column(name = "reg_data", length = 0x300)
	private byte[] data;

	@Column(name = "created")
	private Timestamp created;
	
	@Column(name = "last_seen")
	private Timestamp lastSeen;
	
	@Column(name = "created_ip")
	private String createdIp;
	
	@Column(name = "last_seen_ip")
	private String lastSeenIp;
	
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public String getRegId() {
		return regId;
	}
	public void setRegId(String regId) {
		this.regId = regId;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getScoreName() {
		return scoreName;
	}
	public void setScoreName(String scoreName) {
		this.scoreName = scoreName;
	}
	public String getCountry() {
		return country;
	}
	public void setCountry(String country) {
		this.country = country;
	}
	public byte[] getData() {
		return data;
	}
	public void setData(byte[] data) {
		this.data = data;
	}
	public Timestamp getCreated() {
		return created;
	}
	public void setCreated(Timestamp created) {
		this.created = created;
	}
	public Timestamp getLastSeen() {
		return lastSeen;
	}
	public void setLastSeen(Timestamp lastSeen) {
		this.lastSeen = lastSeen;
	}
	public String getCreatedIp() {
		return createdIp;
	}
	public void setCreatedIp(String createdIp) {
		this.createdIp = createdIp;
	}
	public String getLastSeenIp() {
		return lastSeenIp;
	}
	public void setLastSeenIp(String lastSeenIp) {
		this.lastSeenIp = lastSeenIp;
	}
	
	private int bytesToInt(int offset)
	{
		return (data[offset] & 0xff)
				| ((data[offset + 1] & 0xff) << 8)
				| ((data[offset + 2] & 0xff) << 16)
				| ((data[offset + 3] & 0xff) << 24);
	}
	private int bytesToBCM(int offset)
	{
		// ms
		int t = (data[offset] & 0xff) | ((data[offset + 1] & 0xff) << 8);
		if (t == 0xffff && data[offset + 2] == -1 && data[offset + 3] == -1)
			return -1;
		// sec
		t +=  (data[offset + 2] & 0xff) * 1000;
		// min
		t +=  (data[offset + 3] & 0xff) * 100000;
		return t;
	}
	
	public LapTime getLapTime(int circuit, boolean semiAuto)
	{
		int offset = TrackOffset[circuit] * 4;
		if (offset < 0)
			return null;
		offset += 0xd0 + (semiAuto ? 0x30 : 0);
		int time = bytesToBCM(offset);
		if (time == -1)
			return null;
		byte b = data[offset + 0x90];
		int raceMode = (b & 8) != 0 ? 2	// race
				: (b & 4) != 0 ? 1		// drive/free
				: 0;					// training
		boolean assist = (b & 1) != 0;
		boolean tuned = (b & 2) != 0;
		return new LapTime(circuit, semiAuto, time, raceMode, assist, tuned, false); // TODO arcade
	}
	
	public int getPlayCount() {
		return bytesToInt(0xc8);
	}
	
	public int getDistanceDriven() {
		return bytesToInt(0xcc);
	}
}
