<?php

require('func.php');
$conf = loadconfig('irclogger.config.php');
$dbh = getdbhandle($conf);

$sql = "SELECT DATE(tsMsgTime) AS d, count(*) AS c
          FROM messages, channel
         WHERE fk_ChannelID = pk_ChannelID AND vcChannel = '#hive13'
               AND (vcMsgType='publicMsg' OR vcMsgType='actionMsg')
      GROUP BY YEAR(tsMsgTime), MONTH(tsMsgTime), DAY(tsMsgTime)
      ORDER BY YEAR(tsMsgTime), MONTH(tsMsgTime), DAY(tsMsgTime)";
$res = mysql_query($sql,$dbh);

$first = true;

header("Content-Type: text/plain");
echo "Date,# of Messages\n";
while($row = mysql_fetch_array($res, MYSQL_ASSOC)){
    echo $row['d'].",".$row['c']."\n";
}
?>
