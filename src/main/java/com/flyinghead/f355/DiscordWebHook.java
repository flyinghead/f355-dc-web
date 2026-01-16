/*
	F355 Challenge web server revival
	Copyright (C) 2025 flyinghead

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

import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.Properties;

import org.json.JSONException;
import org.json.JSONObject;

public class DiscordWebHook
{
	private static String IconUrl = null;
	private static String GameName = null;
	private static String WebHookUrl = null;

	private static void sendMessage(String json)
	{
		if (WebHookUrl == null || WebHookUrl.isEmpty())
			return;
		URL url;
		try {
			url = new URL(WebHookUrl);
		} catch (MalformedURLException e) {
			e.printStackTrace();
			return;
		}
		try {
			HttpURLConnection conn= (HttpURLConnection)url.openConnection();
			conn.setDoOutput(true);
			conn.setInstanceFollowRedirects(false);
			conn.setRequestMethod("POST");
			conn.setRequestProperty("Content-Type", "application/json");
			conn.setRequestProperty("User-Agent", "DCNet-DiscordWebhook");	// required!
			byte[] postData = json.getBytes("UTF-8");
			conn.setRequestProperty("Content-Length", Integer.toString(postData.length));
			conn.setUseCaches(false);
			try (DataOutputStream wr = new DataOutputStream(conn.getOutputStream())) {
			   wr.write(postData);
			   wr.close();
			}
			// expected: 204 No Content
			int code = conn.getResponseCode();
			if (code < 200 || code >= 300)
				System.out.println("Discord error: " + conn.getResponseCode() + " " + conn.getResponseMessage());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private static String escape(String s)
	{
		StringBuilder sb = new StringBuilder(s.length());
		for (char c : s.toCharArray())
		{
			if (c == '*' || c == '_' || c == '`' || c == '~' || c == '<'
					|| c == '>' || c == ':' || c == '[' || c == '\\')
				sb.append('\\');
			sb.append(c);
		}
		return sb.toString();
	}

	private static void readConfig()
	{
		if (IconUrl == null)
		{
			try {
				String text = new String(Files.readAllBytes(Paths.get("/usr/local/share/dcnet/games.json")), StandardCharsets.UTF_8);
				JSONObject games = new JSONObject(text);
				JSONObject game = games.getJSONObject("f355");
				if (game != null)
				{
					IconUrl = game.getString("thumbnail");
					GameName = game.getString("name");
				}
			} catch (IOException e) {
				e.printStackTrace();
			} catch (JSONException e) {
				e.printStackTrace();
			}
			if (IconUrl == null)
				IconUrl = "https://dcnet.flyca.st/gamepic/f355.jpg";
			if (GameName == null)
				GameName = "F355 Challenge";
		}
		if (WebHookUrl == null)
		{
			try {
				Properties props = new Properties();
				props.load(new FileInputStream("/usr/local/etc/dcnet/discord.conf"));
				String disabled = props.getProperty("disabled-games");
				if (disabled != null && disabled.indexOf("f355") != -1)
					WebHookUrl = "";
				else
					WebHookUrl = props.getProperty("webhook", System.getProperty("f355.discord.webhook", ""));
			} catch (IOException e) {
				e.printStackTrace();
				WebHookUrl = ""; // Disable discord
			}
		}
	}

	public static void playerWaiting(String playerName, String trackName, String[] allPlayers)
	{
		/*
		{
		  "content": "Player **Fly** is waiting for other racers.\n\n_ _",
		  "embeds": [
		    {
		      "title": "Waiting list",
		      "description": "Fly\John Doe\nJoe Smith",
		      "color": 9118205,
		      "author": {
		        "name": "F355 Challenge",
		        "icon_url": "https://cdn.thegamesdb.net/images/thumb/boxart/front/125181-1.jpg"
		      }
		    }
		  ],
		  "attachments": []
		}
		 */
		readConfig();
		JSONObject j = new JSONObject();
		j.put("content", "Player **" + escape(playerName) + "** is waiting for other racers on circuit **" + trackName + "**.\n_ _");
		JSONObject embed = new JSONObject();
		embed.put("title", "Waiting List");
		embed.put("color", 9118205);
		JSONObject author = new JSONObject();
		author.put("name", GameName);
		author.put("icon_url", IconUrl);
		embed.put("author", author);
		StringBuilder psb = new StringBuilder();
		for (String p : allPlayers)
			psb.append(escape(p)).append('\n');
		embed.put("description", psb.toString());
		j.put("embeds", Collections.singletonList(embed));
		j.put("attachments", Collections.emptyList());
		
		sendMessage(j.toString());
	}
	
	public static void raceStart(String trackName, String[] racers)
	{
		/*
		 {
		   "content": null,
		   "embeds": [
		     {
		       "title": "MONZA: Race Start",
		       "description": "Fly\John Doe\nJoe Smith",
		       "color": 9118205,
	 	       "author": {
	 	         "name": "F355 Challenge",
	 	         "icon_url": "https://cdn.thegamesdb.net/images/thumb/boxart/front/125181-1.jpg"
	 	       }
	 	     }
		   ],
		   "attachments": []
		 }
		*/
		readConfig();
		JSONObject j = new JSONObject();
		JSONObject embed = new JSONObject();
		embed.put("title", trackName + ": Race Start");
		embed.put("color", 9118205);
		JSONObject author = new JSONObject();
		author.put("name", GameName);
		author.put("icon_url", IconUrl);
		embed.put("author", author);
		StringBuilder psb = new StringBuilder();
		for (String p : racers)
			psb.append(escape(p)).append('\n');
		embed.put("description", psb.toString());
		j.put("embeds", Collections.singletonList(embed));
		j.put("attachments", Collections.emptyList());
		
		sendMessage(j.toString());
	}
}
