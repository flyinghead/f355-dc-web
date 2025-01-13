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
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DiscordWebHook
{
	static class Json
	{
		public void addProp(String name, Object value) {
			root.put(name, value);
		}
		
		public String toString() {
			StringBuilder sb = new StringBuilder();
			toJson(sb, root);
			return sb.toString();
		}
		
		private void toJson(StringBuilder sb, List<Object> list)
		{
			sb.append("[ ");
			if (!list.isEmpty())
			{
				for (Object o : list) {
					toJson(sb, o);
					sb.append(", ");
				}
				sb.delete(sb.length() - 2, sb.length());
			}
			sb.append(" ]");
		}

		private void toJson(StringBuilder sb, Map<String, Object> map)
		{
			sb.append("{ ");
			if (!map.isEmpty())
			{
				for (Map.Entry<String, Object> entry : map.entrySet()) {
					sb.append('"').append(entry.getKey()).append("\": ");
					toJson(sb, entry.getValue());
					sb.append(", ");
				}
				sb.delete(sb.length() - 2, sb.length());
			}
			sb.append(" }");
		}

		private void toJson(StringBuilder sb, Number n) {
			sb.append(n.toString());
		}

		private void toJson(StringBuilder sb, Boolean b) {
			sb.append(b.toString());
		}

		private void toJson(StringBuilder sb, String s) {
			sb.append('"');
			for (char c : s.toCharArray()) {
				if (c == '\n')
					sb.append("\\n");
				else if (c == '"')
					sb.append("\\\"");
				else
					sb.append(c);
			}
			sb.append('"');
		}

		private void toJson(StringBuilder sb, Object o) {
			if (o == null)
				sb.append("null");
			else if (o instanceof List<?>)
				toJson(sb, (List<Object>)o);
			else if (o instanceof Number)
				toJson(sb, (Number)o);
			else if (o instanceof Boolean)
				toJson(sb, (Boolean)o);
			else if (o instanceof Map<?, ?>)
				toJson(sb, (Map<String, Object>)o);
			else
				toJson(sb, o.toString());
		}
		private Map<String, Object> root = new HashMap<>();
	};

	private static void sendMessage(String json)
	{
		String webhookUrl = System.getProperty("f355.discord.webhook", null);
		if (webhookUrl == null)
			return;
		URL url;
		try {
			url = new URL(webhookUrl);
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
		Json j = new Json();
		j.addProp("content", "Player **" + playerName + "** is waiting for other racers on circuit **" + trackName + "**.\n_ _");
		Map<String, Object> embed = new HashMap<>();
		embed.put("title", "Waiting List");
		embed.put("color", 9118205);
		Map<String, Object> author = new HashMap<>();
		author.put("name", "F355 Challenge");
		author.put("icon_url", "https://cdn.thegamesdb.net/images/thumb/boxart/front/125181-1.jpg");
		embed.put("author", author);
		StringBuilder psb = new StringBuilder();
		for (String p : allPlayers)
			psb.append(p).append('\n');
		embed.put("description", psb.toString());
		j.addProp("embeds", Collections.singletonList(embed));
		j.addProp("attachments", Collections.emptyList());
		
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
		Json j = new Json();
		Map<String, Object> embed = new HashMap<>();
		embed.put("title", trackName + ": Race Start");
		embed.put("color", 9118205);
		Map<String, Object> author = new HashMap<>();
		author.put("name", "F355 Challenge");
		author.put("icon_url", "https://cdn.thegamesdb.net/images/thumb/boxart/front/125181-1.jpg");
		embed.put("author", author);
		StringBuilder psb = new StringBuilder();
		for (String p : racers)
			psb.append(p).append('\n');
		embed.put("description", psb.toString());
		j.addProp("embeds", Collections.singletonList(embed));
		j.addProp("attachments", Collections.emptyList());
		
		sendMessage(j.toString());
	}
}
