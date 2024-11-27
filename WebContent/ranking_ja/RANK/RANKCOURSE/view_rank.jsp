<%@ page contentType="text/html; charset=x-sjis" %>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.0 Transitional//EN" "http://www.w3.org/TR/REC-html40/loose.dtd">
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<jsp:useBean id="bean" class="com.flyinghead.f355.RankingBean" />
<jsp:setProperty name="bean" property="request" value="${pageContext.request}" />
<head>
   <title>F355-RANKING</title>
   <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
</head>
<body bgcolor="#FFFFFF"> 
<center>

<br>
<br>

<table border="0" cellspacing="0" cellpadding="0" width="512">
   <tr bgcolor="#FF0000">
      <td><img src="../IMAGES/F355_time.gif" width="274" height="50" align="bottom" alt="time"><br><img src="../IMAGES/${bean.headTgif}" width="274" height="20" align="bottom" alt="${bean.circuitNameLow}"></td>
      <td align="right"><img src="../IMAGES/F355_photo.jpg" width="83" height="50" align="bottom" alt="F355 Photo"><img src="../IMAGES/${bean.headgif}" width="88" height="76" align="bottom" alt="${bean.circuitNameLow}"></td>
   </tr>
</table>
<br>

<table border="0" cellspacing="0" cellpadding="0" width="512">
   <tr>
      <td><font color="#000000">●${bean.semiAutoLabel}/${bean.circuitName}</font></td>
   </tr>
</table>
<table border="0" cellpadding="0" width="512">
   <tr bgcolor="#FF0000">
      <td width="50"><center><font color="#FFFFFF">Rank</font></center></td>
      <td width="200"><font color="#FFFFFF">Name</font></td>
      <td width="70"><font color="#FFFFFF">Country</font></td>
      <td width="80"><font color="#FFFFFF">Time</font></td>
      <td width="100"><font color="#FFFFFF">Date</font></td>
   </tr>
   <c:forEach var="result" items="${bean.results}">
	   <tr bgcolor="#FFCCCC">
	      <td width="50"><center>${bean.listIndex}</center></td>
	      <td width="200">${result.player.name}<br>&#91;${result.player.scoreName}&#93;
	      <c:if test="${result.tuned}">
	        <img src="../../IMAGES/icon_tuned.gif" vspace="2" hspace="5" alt="tuned">
	      </c:if>
	      <c:if test="${result.assisted}">
	        <img src="../../IMAGES/icon_assisted.gif" vspace="2" hspace="5" alt="assisted">
	      </c:if>
	      <c:if test="${result.arcade}">
	        <img src="../../IMAGES/icon_arcade.gif" vspace="2" hspace="5" alt="arcade">
	      </c:if>
	      <img src="../../IMAGES/${bean.getRaceModeIcon(result)}" vspace="2" hspace="5" alt="${bean.getRaceModeAlt(result)}"></td>
	      <td width="70">${result.player.country}</td>
	      <td width="80">${result.runTimeString}</td>
	      <td width="100">${result.runDateString}</td>
	   </tr>
   </c:forEach>
<tr bgcolor="#FF0000"><th colspan="5">F355 challenge</th></tr>
</table></center><br>
<center>
<img src="../../IMAGES/icon_tuned.gif" hspace="10" alt="tuned">
<img src="../../IMAGES/icon_assisted.gif" hspace="10" alt="assisted">
<img src="../../IMAGES/icon_free.gif" hspace="10" alt="free">
<img src="../../IMAGES/icon_arcade.gif" hspace="10" alt="arcade">
<a href="mark.html">マークの説明</a><p>
</center>
<br>
<center><table><tr><td width="480">順位を示す数字が赤い場合は、そのタイムを出した際のゴーストーカーデータをダウンロード出来ます。実際にダウンロードするには、Dreamcast からアクセスする必要があります。<p></td></tr></table></center><center>
<table>
<tr><td width="10"></td><td width="160"></td><td width="170"></td><td width="170"></td></tr>
<tr>
 <td></td>
 <td align="left"><c:if test="${not empty bean.previousPageUrl}"><a href="${bean.previousPageUrl}">previous</a></c:if></td>
 <td align="center"><a href="index.html">up</a></td>
 <td align="right"><c:if test="${not empty bean.nextPageUrl}"><a href="${bean.nextPageUrl}">next</a></c:if></td>
</tr>
</table>
</center>
<br>
<center>
<form action="view_rank.jsp" method="get">
  <input type="hidden" name="circuit" value="${bean.circuit}">
  <input type="hidden" name="semiAuto" value="${param['semiAuto']}">
  <input type="text" maxlength="9" name="index" size="10" value="${bean.index}">位からのランキングｰ<p>
  <select name="tuned">
    <option value="-1" ${bean.tuned == -1 ? "selected" : "" }>ノーマルとチューンドの両方
    <option value="0" ${bean.tuned == 0 ? "selected" : "" }>ノーマルのみ
    <option value="1" ${bean.tuned == 1 ? "selected" : "" }>チューンドのみ
  </select><p>
  <select name="assisted">
    <option value="-1" ${bean.assisted == -1 ? "selected" : "" }>アシスト機能オンとオフの両方
    <option value="0" ${bean.assisted == 0 ? "selected" : "" }>アシスト機能オフのみ
    <option value="1" ${bean.assisted == 1 ? "selected" : "" }>アシスト機能オンのみ
  </select><p>
  <select name="raceMode">
    <option value="-1" ${bean.raceMode == -1 ? "selected" : "" }>全てのモード（トレーニング、フリー走行、レース）
    <option value="0" ${bean.raceMode == 0 ? "selected" : "" }>レース以外のモード（トレーニング、フリー走行）のみ
    <option value="1" ${bean.raceMode == 1 ? "selected" : "" }>レースモードのみ
  </select><p>
  <select name="country">
    <option value="-1" ${bean.country == -1 ? "selected" : "" }>All countries
    <option value="0" ${bean.country == 0 ? "selected" : "" }>Japan (JP)
    <option value="1" ${bean.country == 1 ? "selected" : "" }>North and Middle America (US,CA,MX)
    <option value="2" ${bean.country == 2 ? "selected" : "" }>Europe (UK,FR,DE,ES,IT,IS,FI,NO,SE,NL,LU,BE,AT,CH,GR,PT,IE)
    <option value="3" ${bean.country == 3 ? "selected" : "" }>--
  </select><p>
  <select name="machine">
    <option value="-1" ${bean.machine == -1 ? "selected" : "" }>ドリームキャスト版とアーケード版の両方
    <option value="0" ${bean.machine == 0 ? "selected" : "" }>ドリームキャスト版のみ
    <option value="1" ${bean.machine == 1 ? "selected" : "" }>アーケード版のみ
  </select><p><br>
  <table cellpadding="5"><tr><td><input type="submit" value="以上の条件でランキングを見る"></td><td><input type="reset" value="入力をクリア"></td></tr></table>
</form>
</center><center><a href="../../../jp"><img src="../../IMAGES/355b.gif" border="0" alt="Home"></a></center></body>
</html>
<!--
     FILE ARCHIVED ON 18:23:19 Sep 08, 2004 AND RETRIEVED FROM THE
     INTERNET ARCHIVE ON 20:05:46 Dec 12, 2023.
     JAVASCRIPT APPENDED BY WAYBACK MACHINE, COPYRIGHT INTERNET ARCHIVE.

     ALL OTHER CONTENT MAY ALSO BE PROTECTED BY COPYRIGHT (17 U.S.C.
     SECTION 108(a)(3)).
-->
