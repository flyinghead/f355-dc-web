<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.0 Transitional//EN" "http://www.w3.org/TR/REC-html40/loose.dtd">
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<html>
<head>
   <title>REGISTER TIME</title>
   <meta http-equiv="Content-Type" content="text/html; charset=iso-8859-1">
</head>
<body bgcolor="#FFFFFF"> 
<br>
<center><img src="http://www.sega-rd2.com/f355/ranking_en/IMAGES/top_image.gif" width="336" height="94" align="bottom"><br>
<font size="+2" color="#000066"><b>Welcome ${bean.playerName}!</b></font>
<br>
<br>
<font color="#000066"><b>Register your best lap times to the "F355 Challenge" Internet Ranking.</b></font><font color="#FF3300"><b><br>
</b></font></center>

<br>
<br>
<form action="" method="post">
<b><br>
   <input type="hidden" name="playerId" value="${bean.player.id}">
   <input type="hidden" name="country" value="${bean.country}">
   <p><table border="0" bgcolor="#FF0000" cellspacing="0" cellpadding="2">
      <tr>
         <td nowrap>
           <font color="#FFFFFF"><b>Player Information</b></font>
         </td>
      </tr>
   </table>
    </b>

   <table border="0" cellspacing="0" cellpadding="3">
      <tr>
         <td>Name</td><td nowrap><b>${bean.playerName}</b></td>
      </tr>
      <tr>
         <td>Country</td><td nowrap><b>${bean.player.country}</b></td>
      </tr>
      <tr>
         <td nowrap>Score Name</td>
         <td><input type="text" maxlength="3" name="scoreName" value="${bean.player.scoreName}"></td>
         <td><input type="submit" name="update" value="Update"></td>
      </tr>
      <tr>
         <td nowrap>Play Count</td><td nowrap><b>${bean.raceCount}</b></td>
      </tr>
      <tr>
         <td nowrap>Distance Driven</td><td nowrap><b>${bean.distanceMiles} mi</b></td>
      </tr>
   </table>
 
 <p><table border="0" bgcolor="#FF0000" cellspacing="0" cellpadding="2">
      <tr>
         <td nowrap>
           <font color="#FFFFFF"><b>Best Lap Times</b></font>
         </td>
      </tr>
   </table>
 
    <table border="0" cellspacing="0" cellpadding="3">
      <c:forEach items="${bean.tracks}" var="track">
	      <tr bgcolor="#FFCCCC">
	         <td>${bean.getTrackName(track)}</td>
	         <td>AT</td>
	         <c:if test="${bean.hasResult(track, false)}">
		         <!-- td nowrap><b>${bean.getLapTime(track, false)}</b> -->
		         <td nowrap><input type="submit" name="atTrack${track}" value="${bean.getLapTime(track, false)}"></b>
		         	<img src="http://www.sega-rd2.com/f355/ranking_en/IMAGES/${bean.getRaceModeIcon(track, false)}" vspace="2" hspace="2">
		         	<c:if test="${bean.isAssist(track, false)}">
		         		<img src="http://www.sega-rd2.com/f355/ranking_en/IMAGES/icon_assisted.gif" vspace="2" hspace="2" alt="assisted">
		         	</c:if>
		         	<c:if test="${bean.isTuned(track, false)}">
		         		<img src="http://www.sega-rd2.com/f355/ranking_en/IMAGES/icon_tuned.gif" vspace="2" hspace="2" alt="tuned">
		         	</c:if>
		         </td>
		     </c:if>
	      </tr>
	      <tr>
	         <td></td>
	         <td>SA</td>
	         <c:if test="${bean.hasResult(track, true)}">
		         <!-- td nowrap><b>${bean.getLapTime(track, true)}</b>  -->
		         <td nowrap><input type="submit" name="saTrack${track}" value="${bean.getLapTime(track, true)}"></b>
		         	<img src="http://www.sega-rd2.com/f355/ranking_en/IMAGES/${bean.getRaceModeIcon(track, true)}" vspace="2" hspace="2">
		         	<c:if test="${bean.isAssist(track, true)}">
		         		<img src="http://www.sega-rd2.com/f355/ranking_en/IMAGES/icon_assisted.gif" vspace="2" hspace="2" alt="assisted">
		         	</c:if>
		         	<c:if test="${bean.isTuned(track, true)}">
		         		<img src="http://www.sega-rd2.com/f355/ranking_en/IMAGES/icon_tuned.gif" vspace="2" hspace="2" alt="tuned">
		         	</c:if>
		         </td>
	    	 </c:if>
	      </tr>
      </c:forEach>
    </table>
</form> 
<center><a href="http://www.sega-rd2.com/f355/ranking_${bean.country}/RANK/RANKCOURSE/index.html"><b>See all rankings</b></a></center>
</body>
</html>
