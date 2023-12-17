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

import java.io.File;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.persistence.NoResultException;

import org.hibernate.BaseSessionEventListener;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.query.Query;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.flyinghead.f355.F355;

@Repository
@Service
public class DbService implements IDbService
{
	@Autowired
	private SessionFactory sessionFactory;

	class TransactionalFileDeleter extends BaseSessionEventListener
	{
		private static final long serialVersionUID = 1L;
		private List<File> toDelete;

		TransactionalFileDeleter(Session session, File file) {
			toDelete = Collections.singletonList(file);
			session.addEventListeners(this);
		}

		TransactionalFileDeleter(Session session, List<File> file) {
			toDelete = file;
			session.addEventListeners(this);
		}

		@Override
		public void transactionCompletion(boolean successful) {
			if (!successful)
				return;
			for (File f : toDelete)
				f.delete();
		}
	}

	@Override
	@Transactional
	public Player getPlayer(int playerId)
	{
		Session session = sessionFactory.getCurrentSession();
		return session.get(Player.class, playerId);
	}

	private String getRegId(byte[] data)
	{
		if (data.length != 0x300)
			throw new RuntimeException("Invalid registration data");
		return new String(data, 0, 16);
	}
	
	private Player getPlayer(String regId)
	{
		Session session = sessionFactory.getCurrentSession();
		
		Query<Player> query = session.createQuery("from Player where regId = :regId", Player.class)
				.setParameter("regId", regId);
		try {
			return query.getSingleResult();
		} catch (NoResultException e) {
			return null;
		}
	}

	@Override
	@Transactional
	public Player createUpdatePlayer(byte[] data, String ip)
	{
		String regId = getRegId(data);
		Session session = sessionFactory.getCurrentSession();
		
		Player player = getPlayer(regId);
		if (player == null)
		{
			player = new Player();
			player.setRegId(regId);
			player.setCreated(new Timestamp(System.currentTimeMillis()));
			player.setCreatedIp(ip);
		}
		player.setName(new String(data, 0x24, 12).trim());
		if (player.getScoreName() == null)
			player.setScoreName(new String(data, 0x20, 3).trim());
		player.setCountry(new String(data, 0x30, 3).trim());
		player.setData(data);
		player.setLastSeen(new Timestamp(System.currentTimeMillis()));
		player.setLastSeenIp(ip);
		player = (Player)session.merge(player);
		return player;
	}

	@Override
	@Transactional
	public Player updateScoreName(int playerId, String name, String ip)
	{
		Session session = sessionFactory.getCurrentSession();
		Player player = session.get(Player.class, playerId);
		if (player == null)
			throw new RuntimeException("Player not found");
		player.setScoreName(name);
		player.setLastSeen(new Timestamp(System.currentTimeMillis()));
		player.setLastSeenIp(ip);
		session.merge(player);
		return player;
	}

	@Override
	@Transactional
	public Player saveBestLap(int playerId, int circuit, boolean semiAuto)
	{
		Session session = sessionFactory.getCurrentSession();
		Player player = session.get(Player.class, playerId);
		if (player == null)
			throw new RuntimeException("Player not found");
		LapTime lapTime = player.getLapTime(circuit, semiAuto);
		if (lapTime == null)
			throw new RuntimeException("No time available");
		
		Query<Result> rQuery = session.createQuery("from Result where player = :player and circuit = :circuit and semiAuto = :semiAuto", Result.class)
				.setParameter("player", player)
				.setParameter("circuit", circuit)
				.setParameter("semiAuto", semiAuto);
		List<Result> list = rQuery.getResultList();
		final List<File> toDelete = new ArrayList<>();
		for (Result r : list)
		{
			if (r.getRunTime() <= lapTime.getTime())
				// Better or same time already exist for this user
				return player;
			if (r.getDataPath() != null)
				toDelete.add(new File(F355.getFileStore(), r.getDataPath()));
			session.delete(r);
		}
		if (!toDelete.isEmpty())
			new TransactionalFileDeleter(session, toDelete);

		Result result = new Result();
		result.setPlayer(player);
		result.setRunDate(new Timestamp(System.currentTimeMillis()));
		result.setCircuit(circuit);
		result.setRaceMode(lapTime.getRaceMode());
		result.setRunTime(lapTime.getTime());
		result.setSemiAuto(semiAuto);
		result.setArcade(lapTime.isArcade());
		result.setAssisted(lapTime.isAssist());
		result.setTuned(lapTime.isTuned());
		session.merge(result);
		
		return player;
	}

	@Override
	@Transactional
	public Player saveResult(int playerId, byte[] data, String fileName, String ip)
	{
		String regId = new String(data, 0x290, 16);
		Session session = sessionFactory.getCurrentSession();
		Player player = session.get(Player.class, playerId);
		if (player == null)
			throw new RuntimeException("Player not found");
		if (!regId.equals(player.getRegId()))
			throw new RuntimeException("Invalid driving data user id");
		
		int circuit = getCircuitId(data);
		boolean semiAuto = isSemiAuto(data);
		int time = getRunTime(data);
		int raceMode = getRaceMode(data);
		Query<Result> rQuery = session.createQuery("from Result where player = :player and circuit = :circuit and semiAuto = :semiAuto", Result.class)
				.setParameter("player", player)
				.setParameter("circuit", circuit)
				.setParameter("semiAuto", semiAuto);
		List<Result> list = rQuery.getResultList();
		if (list.isEmpty() || list.get(0).getRunTime() != time || list.get(0).getRaceMode() != raceMode)
			throw new RuntimeException("Unknown driving data");
		
		Result result = list.get(0);
		if (result.getDataPath() != null)
			new TransactionalFileDeleter(session, new File(F355.getFileStore(), result.getDataPath()));

		result.setDataPath(fileName);
		// tuned? F355DATA.SGO is tuned
		// assisted? F355DATA.MNZ is without assist
		// arcade: offset 0x287: 0=arcade (f355, twin), 66=dc (us, eu, jp), 47=twin 2 ?
		//if (result.getCircuit() >= 6 || data[0x287] != 0x66)
		//	result.setArcade(true);
		session.merge(result);
		
		return player;
	}
	
	@Override
	@Transactional
	public List<Result> getResults(int circuit, boolean semiAuto, int index, int count, int country, int assisted, int tuned, int raceMode, int arcade)
	{
		Session session = sessionFactory.getCurrentSession();
		
		// TODO ties!!
		StringBuilder sb = new StringBuilder("from Result where circuit = :circuit and semiAuto = :semiAuto");
		switch (country)
		{
		case JAPAN:
			sb.append(" and player.country = 'JP'");
			break;
		case AMERICA:
			sb.append(" and player.country in ('US', 'CA', 'MX')");
			break;
		case EUROPE:
			sb.append(" and player.country in ('UK', 'FR', 'DE', 'ES', 'IT', 'IS', 'FI', 'NO', 'SE',"
					+ " 'NL', 'LU', 'BE', 'AT', 'CH', 'GR', 'PT', 'IE')");
			break;
		case NOT_SET:
			sb.append(" and player.country = '--'");
			break;
		}
		if (assisted == 0)
			sb.append(" and assisted = false");
		else if (assisted == 1)
			sb.append(" and assisted = true");
		if (tuned == 0)
			sb.append(" and tuned = false");
		else if (tuned == 1)
			sb.append(" and tuned = true");
		if (raceMode == TRAINING_DRIVING)
			sb.append(" and raceMode != 2");
		else if (raceMode == RACE)
			sb.append(" and raceMode = 2");
		if (arcade == 0)
			sb.append(" and arcade = false");
		else if (arcade == 1)
			sb.append(" and arcade = true");
		
		sb.append(" order by runTime, runDate");
			
		Query<Result> query = session.createQuery(sb.toString(), Result.class)
				.setParameter("circuit", circuit)
				.setParameter("semiAuto", semiAuto)
				.setFirstResult(Math.max(0, index))
				.setMaxResults(count);
		return query.getResultList();
	}

	private int getCircuitId(byte[] data)
	{
		String circuitName = new String(data, 31, 12);
		if (circuitName.startsWith("SUZUKA SHORT"))
			return 0;
		if (circuitName.startsWith("MOTEGI"))
			return 1;
		if (circuitName.startsWith("SUZUKA"))
			return 2;
		if (circuitName.startsWith("LONG-BEACH"))
			return 3;
		if (circuitName.startsWith("SUGO"))
			return 4;
		if (circuitName.startsWith("MONZA"))
			return 5;
		if (circuitName.startsWith("FIORANO"))
			return 7;
		if (circuitName.startsWith("NURBURGRING"))
			return 8;
		if (circuitName.startsWith("LAGUNA-SECA"))
			return 9;
		if (circuitName.startsWith("SEPANG"))
			return 10;
		if (circuitName.startsWith("ATLANTA"))
			return 11;
		
		throw new RuntimeException("Unknown circuit");
	}
	
	// in "minute coded decimal"
	private int getRunTime(byte[] data)
	{
		return (data[0] - '0') * 100000
				+ (data[2] - '0') * 10000
				+ (data[3] - '0') * 1000
				+ (data[5] - '0') * 100
				+ (data[6] - '0') * 10
				+ data[7] - '0';
	}
	
	private boolean isSemiAuto(byte[] data)
	{
		return data[10] == 'S' && data[11] == 'A';
	}
	
	private int getRaceMode(byte[] data)
	{
		switch (data[8])
		{
		case 'T':
			return 0;
		case 'F':
		default:
			return 1;
		case 'R':
			return 2;
		}
	}

}
