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
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Base64;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.springframework.beans.factory.annotation.Autowired;

import com.flyinghead.f355.db.IDbService;
import com.flyinghead.f355.db.Player;

@WebServlet("/cgi-bin/f355/dp3_player_replay.cgi")
public class PlayerReplayServlet extends AutowiredServlet
{
	private static final long serialVersionUID = 1L;

	@Autowired
    private IDbService dbService;

	/**
	 * The multipart/form-data sent by the dreamcast is incorrect: the boundary when used is not prepended by two hyphens ("--").
	 * So we remove the first two hyphens of the boundary definition.
s	 *
	 */
	class RequestFixer extends HttpServletRequestWrapper
	{
		public RequestFixer(HttpServletRequest request) throws IOException
		{
			super(request);
			contentType = request.getContentType();
			if (contentType != null)
			{
				int i = contentType.indexOf("boundary=");
				if (i != -1)
				{
					i += 9;
					contentType = contentType.substring(0, i) + contentType.substring(i + 2);
				}
			}
		}

		@Override
		public String getHeader(String name) {
			if ("Content-type".equalsIgnoreCase(name))
				return contentType;
			else
				return super.getHeader(name);
		}

		@Override
		public Enumeration<String> getHeaders(String name) {
			if ("Content-type".equalsIgnoreCase(name))
				return Collections.enumeration(Collections.singletonList(contentType));
			else
				return super.getHeaders(name);
		}

		@Override
		public String getContentType() {
			return contentType;
		}
		
		private String contentType;
	}
	
	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException
	{
		String userAgent = req.getHeader("User-Agent");
		if (userAgent != null && (userAgent.contains("DreamKey") || userAgent.contains("DreamPassport")))
			req = new RequestFixer(req);
		if (!ServletFileUpload.isMultipartContent(req))
			throw new ServletException("Not multipart content");
		ServletFileUpload upload = new ServletFileUpload(new DiskFileItemFactory());
		try {
			int playerId = -1;
			String country = "en";
			byte[] data = null;
			List<FileItem> items = upload.parseRequest(req);
			for (FileItem item : items)
			{
				if (item.getFieldName().equals("playerId")) {
					playerId = Integer.valueOf(item.getString());
					continue;
				}
				if (item.getFieldName().equals("country")) {
					country = item.getString();
					continue;
				}
				if (!"thefile".equals(item.getFieldName()))
					continue;

				String content = new String(item.get(), "UTF-8");
				int eol = content.indexOf("\n\n");
				if (eol == -1)
					throw new IOException("EOL not found in thefile");

				content = content.substring(eol + 2);
				content = Base64Scramble.unscramble(content);
				data = Base64.getMimeDecoder().decode(content);
			}
			if (data == null)
				throw new ServletException("Result data not found");
			
			File file = null;
			String fileName = null;
			do {
				fileName = System.currentTimeMillis() + ".bin";
				file = new File(F355.getFileStore(), fileName);
			} while (file.exists());
			log("Saving to " + file.getPath() + " (" + data.length + " bytes)");
			FileOutputStream out = new FileOutputStream(file);
			out.write(data);
			out.close();
			
			RegisterBean bean = new RegisterBean();
			Player player = null;
			try {
				player = dbService.saveResult(playerId, data, fileName, getRemoteIP(req));
				bean.setUploadMessage("Upload successful!");
			} catch (RuntimeException e) {
				file.delete();
				log("saveResult failed", e);
				bean.setUploadMessage("Upload failed: " + e.getMessage());
			}
			if (player == null)
				player = dbService.getPlayer(playerId);
			bean.setPlayer(player);
			bean.setCountry(country);
			req.setAttribute("bean", bean);
			RequestDispatcher reqDispatcher = req.getRequestDispatcher("/ranking_" + country + "/RANK/RANKCOURSE/register_time.jsp");
			reqDispatcher.forward(req, resp);
		} catch (IOException e) {
			throw e;
		} catch (Exception e) {
			throw new IOException(e);
		}
	}
}
