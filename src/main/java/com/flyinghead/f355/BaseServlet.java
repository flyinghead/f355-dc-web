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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Random;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public abstract class BaseServlet extends HttpServlet
{
	private static final long serialVersionUID = 1L;
	
	protected static Random random = new Random();

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException
	{
		InputStream in = req.getInputStream();
		byte[] buf = new byte[1024];
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		int readLen;
		while ((readLen = in.read(buf, 0, buf.length)) != -1)
			out.write(buf, 0, readLen);
		byte[] data = out.toByteArray();

		if (data.length < 3) {
			respondError(1, resp);
			return;
		}
		int crc = CRC16.crc16(data, 3, data.length - 3);
		int recvcrc = (data[1] & 0xff) + ((data[2] & 0xff) << 8);
		if (crc != recvcrc)
		{
			log("Invalid crc: expected " + Integer.toHexString(crc) + " got " + Integer.toHexString(recvcrc) + " (" + data.length + " bytes)");
			resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			return;
		}
		process(data, req, resp);
	}
	
	protected void respond(byte[] data, HttpServletResponse resp) throws IOException
	{
		ServletOutputStream out = resp.getOutputStream();
		out.write(0);
		int crc = CRC16.crc16(data);
		out.write(crc & 0xff);
		out.write((crc >> 8) & 0xff);
		out.write(data);
	}

	protected void respondError(int error, HttpServletResponse resp) throws IOException {
		ServletOutputStream out = resp.getOutputStream();
		out.write((byte)error);
	}

	protected abstract void process(byte[] input, HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException;
	
	protected Races getRaces() {
		return (Races)getServletContext().getAttribute("races");
	}
	
	protected int makeId() {
		return random.nextInt(10000000); // MUST be less than 10 million for correct qualifier ranking
	}
	
	protected void idToBytes(int id, byte[] array, int offset)
	{
		array[offset] = (byte)id;
		array[offset + 1] = (byte)(id >> 8);
		array[offset + 2] = (byte)(id >> 16);
		array[offset + 3] = (byte)(id >> 24);
	}
	
	protected int bytesToId(byte[] array, int offset)
	{
		return (array[offset] & 0xff)
				| ((array[offset + 1] & 0xff) << 8)
				| ((array[offset + 2] & 0xff) << 16)
				| ((array[offset + 3] & 0xff) << 24);
	}
}
