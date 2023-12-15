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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet("/cgi-bin/f355/download/*")
public class DownloadServlet extends AutowiredServlet
{
	private static final long serialVersionUID = 1L;

	private static final String Extension[] = {
		"SZS", "MTG", "SZK", "LNG", "SGO", "MNZ",
		"MRN", "FIO", "NUR", "LAG", "SEP", "ATL" 
	};

	private File getTmpDir() {
		return new File(System.getProperty("java.io.tmpdir"));
	}
	
	private void serveVmFile(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException
	{
		String filename = req.getPathInfo().substring(1).replaceAll("/\\:", "");
		File file = new File(getTmpDir(), filename);
		if (!file.exists()) {
			resp.sendError(HttpServletResponse.SC_NOT_FOUND);
			return;
		}
		if (file.getName().endsWith(".VMI"))
			resp.setContentType("application/x-dreamcast-vms-info");
		else
			resp.setContentType("application/x-dreamcast-vms");
		
		FileInputStream in = new FileInputStream(file);
		OutputStream out = resp.getOutputStream();
		byte buffer[] = new byte[4096];
		try {
			for (;;)
			{
				int l = in.read(buffer);
				if (l == -1)
					break;
				out.write(buffer, 0, l);
			}
		} finally {
			in.close();
		}
	}
	
	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException
	{
		String pathInfo = req.getPathInfo();
		if (pathInfo != null && (pathInfo.endsWith(".VMI") || pathInfo.endsWith(".VMS"))) {
			serveVmFile(req, resp);
			return;
		}
		String filename = req.getParameter("file");
		int circuit = Integer.valueOf(req.getParameter("circuit"));
		filename = filename.replaceAll("[/\\:]", "");
		File file = new File(F355.getFileStore(), filename);
		if (!file.canRead()) {
			resp.sendError(HttpServletResponse.SC_NOT_FOUND);
			return;
		}
		// VMS
		String attachname = "F355DATA." + Extension[circuit];
		String id = String.valueOf(F355.getRandomInt(100000000));
		File vmsfile = new File(getTmpDir(), id + ".VMS");
		FileOutputStream fout = new FileOutputStream(vmsfile);
		FileInputStream in = new FileInputStream(file);
		byte buffer[] = new byte[4096];
		String description = null;
		try {
			for (;;)
			{
				int l = in.read(buffer);
				if (l == -1)
					break;
				if (description == null && l > 32)
					description = new String(buffer, 16, 32);
				fout.write(buffer, 0, l);
			}
		} finally {
			in.close();
			fout.close();
		}
		// VMI
		byte[] vmi = makeVmi(file, attachname, id, description);
		File vmifile = new File(getTmpDir(), id + ".VMI");
		fout = new FileOutputStream(vmifile);
		try {
			fout.write(vmi);
		} finally {
			fout.close();
		}
		
		resp.sendRedirect(req.getContextPath() + "/cgi-bin/f355/download/" + vmifile.getName());
	}

	private byte[] makeVmi(File file, String name, String id, String description)
	{
		long size = file.length();
		byte[] vmi = new byte[0x6c];
		vmi[0] = (byte)(id.charAt(0) & 'S');
		vmi[1] = (byte)(id.charAt(1) & 'E');
		vmi[2] = (byte)(id.charAt(2) & 'G');
		vmi[3] = (byte)(id.charAt(3) & 'A');
		System.arraycopy(description.getBytes(), 0, vmi, 4, description.length());
		System.arraycopy("SEGA ENTERPRISES".getBytes(), 0, vmi, 0x24, 16);
		// date/time
		vmi[0x44] = (byte)(2023 & 0xff);
		vmi[0x45] = 2023 >> 8;
		vmi[0x46] = 12;
		vmi[0x47] = 12;
		vmi[0x48] = 9;
		vmi[0x49] = 33;
		vmi[0x4a] = 00;
		vmi[0x4b] = 2;
		// vmi version (2)
		vmi[0x4d] = 1;
		// file number (2)
		vmi[0x4e] = 1;
		// vmi resource name
		System.arraycopy(id.getBytes(), 0, vmi, 0x50, 8);
		// file name
		System.arraycopy(name.getBytes(), 0, vmi, 0x58, 12);
		// file size
		vmi[0x68] = (byte)(size & 0xff);
		vmi[0x69] = (byte)((size >> 8) & 0xff);
		vmi[0x6a] = (byte)((size >> 16) & 0xff);
		vmi[0x6b] = (byte)((size >> 24) & 0xff);
		
		return vmi;
	}
}
