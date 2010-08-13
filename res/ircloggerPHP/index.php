<?php

require("func.php");
$conf = loadconfig('irclogger.config.php');

header('Content-Type: text/html; charset=utf-8');
?>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN"
 "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en" lang="en" dir="ltr">
<head>
    <title>IRC channel log</title>
    <meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
    <link rel="stylesheet" media="all" type="text/css" href="style.css" />
</head>
<body>
<?php
$dbh = getdbhandle($conf);
$ircChan = mysql_real_escape_string($conf['irc_channel']);
$ircServer = mysql_real_escape_string($conf['irc_server']);

if(isSet($_REQUEST['s'])){
    $sql = "SELECT pk_MessageID, DATE(tsMsgTime) as d, TIME(tsMsgTime) as t, 
                  vcUsername, vcMsgType, vcMessage, 
                  match(vcMessage) 
                  AGAINST ('*".addslashes($_REQUEST['s'])."*' IN BOOLEAN MODE) AS rank 
           FROM messages, channel 
           WHERE fk_ChannelID = pk_ChannelID AND vcChannel = '".$ircChan."' 
                 AND vcServer='".$ircServer."' AND 
                 MATCH(vcMessage) 
                 AGAINST ('*".addslashes($_REQUEST['s'])."*' IN BOOLEAN MODE) 
           ORDER BY rank DESC, tsMsgTime DESC, pk_MessageID DESC";
}elseif(isSet($_REQUEST['u'])){
    $sql = "SELECT tsMsgTime
              FROM messages, channel
             WHERE fk_ChannelID = pk_ChannelID AND vcChannel = '".$ircChan."' AND vcServer = '".$ircServer."' AND 
                   vcUsername = '".addslashes($_REQUEST['u'])."'
          ORDER BY tsMsgTime DESC, pk_MessageID DESC
             LIMIT 1";
    $res = mysql_query($sql,$dbh);
    $row = mysql_fetch_array($res, MYSQL_ASSOC);
    if($row['tsMsgTime']){
        $sql = "SELECT pk_MessageID, DATE(tsMsgTime) as d, TIME(tsMsgTime) as t, vcUsername, vcMsgType, vcMessage
              FROM messages, channel
             WHERE fk_ChannelID = pk_ChannelID AND vcChannel = '".$ircChan."' AND vcServer = '".$ircServer."' AND 
                   tsMsgTime >= '".addslashes($row['tsMsgTime'])."'
          ORDER BY tsMsgTime, pk_MessageID";
    }else{
        echo 'Could not find any logs for user '.htmlspecialchars($_REQUEST['u']);
        $sql = '';
    }
}else{
    if(isSet($_REQUEST['d'])){
        $date = $_REQUEST['d'];
    }else{
        $date = date('Y-m-d');
    }

    $sql = "SELECT pk_MessageID, DATE(tsMsgTime) as d, TIME(tsMsgTime) as t, vcUsername, vcMsgType, vcMessage
              FROM messages, channel
             WHERE fk_ChannelID = pk_ChannelID AND vcChannel = '".$ircChan."' AND vcServer = '".$ircServer."' AND 
                   DATE(tsMsgTime) = DATE('".addslashes($date)."')
          ORDER BY tsMsgTime, pk_ChannelID";
}

if($sql) $res = mysql_query($sql,$dbh);

if(isSet($date)){
    echo '<form action="index.php">';
    echo '<h1>Log for <input type="text" name="d" value="'.htmlspecialchars($date).'" /></h1>';
    echo '</form>';
}elseif($sql && isSet($_REQUEST['u'])){
    echo '<h1>Log since last login of '.htmlspecialchars($_REQUEST['u']).'</h1>';
}elseif($sql && isSet($_REQUEST['s'])){
    echo '<h1>Matching lines for '.htmlspecialchars($_REQUEST['s']).'</h1>';
    echo '<p>Click the timestamp to see the line in context.</p>';
}

echo '<ul id="log">';
if($sql) while($row = mysql_fetch_array($res, MYSQL_ASSOC)){
    echo '<li>';
    echo '<a id="msg'.$row['pk_MessageID'].'" href="index.php?d='.$row['d'].'#msg'.$row['pk_MessageID'].'" class="time">';
    echo '['.$row['t'].']';
    echo '</a>';
    $listItem = '';
    if($row['vcMsgType'] == 'publicMsg'){
        $listItem = '<b style="color:#'.substr(md5($row['vcUsername']),0,6).'">'.htmlspecialchars($row['vcUsername']).'</b><span class="user">';
        $listItem .= htmlspecialchars($row['vcMessage']).'</span></li>';
    } else if($row['vcMsgType'] == 'actionMsg') {
        $listItem  = '<b>&gt;&gt;&gt;</b><span class="user" >';
        $listItem .= '<strong style="color:#'.substr(md5($row['vcUsername']),0,6).'">';
        $listItem .= htmlspecialchars($row['vcUsername']).' '.htmlspecialchars($row['vcMessage']);
        $listItem .= '</strong>';
        $listItem .= '</span></li>';
    } else if($row['vcMsgType'] == 'joinMsg') {
        $listItem  = '<b>*</b><span class="server">';
        $listItem .= htmlspecialchars($row['vcUsername']).' joined the channel.';
        $listItem .= '</span></li>';
    } else if($row['vcMsgType'] == 'partMsg' || $row['vcMsgType'] == 'quitMsg') {
        $listItem = '<b>*</b><span class="server">';
        $listItem .= htmlspecialchars($row['vcUsername']).' left the channel.';
        $listItem .= '</span></li>';
    } else if($row['vcMsgType'] == 'nickChange') {
        $listItem = '<b>*</b><span class="server">';
        $listItem .= htmlspecialchars($row['vcUsername']).' changed their nick to'.htmlspecialchars($row['vcMessage']).'.';
        $listItem .= '</span></li>';
    } else{
        $listItem = '<b>*</b><span class="server">';
        $listItem .= htmlspecialchars($row['vcMessage']).'</span></li>';
    }
    
    $listItem = preg_replace_callback('/((https?|ftp):\/\/[\w-?&;#~=\.\/\@%:]+[\w\/])/ui',
                                 'format_link',$listItem);

    echo $listItem;

}
echo '</ul>';

$sql = "SELECT DISTINCT DATE(tsMsgTime) as d, DAY(tsMsgTime) as day
          FROM messages, channel
         WHERE fk_ChannelID = pk_ChannelID AND vcChannel = '".$ircChan."' AND vcServer = '".$ircServer."' AND 
               tsMsgTime > DATE_SUB(NOW(), INTERVAL 30 DAY)
      ORDER BY tsMsgTime";
$res = mysql_query($sql,$dbh);

echo '<div class="archive">Last 30 days: ';
while($row = mysql_fetch_array($res, MYSQL_ASSOC)){
    $archiveLink = '<a ';
    if(isSet($_REQUEST['d']) && $_REQUEST['d'] == $row['d'])
      $archiveLink .= 'class="current" ';
    echo $archiveLink.'href="index.php?d='.$row['d'].'" >'.$row['day'].'</a> ';
}
echo '</div>';

?>

<div class="footer"><div>
<ul>
    <li><a href="index.php?d=<?php echo date('Y-m-d')?>">Today's log</a></li>
    <li><a href="index.php?d=<?php echo date('Y-m-d',time()-(60*60*24))?>">Yesterday's log</a></li>
</ul>
<ul>
    <li>What happened since your last login?<br />
        <form action="index.php"><input name="u" /></form>
        <small>(Give your nick name and hit enter)</small></li>
    <li>Search:<br />
        <form action="index.php"><input name="s" /></form>
        <small>(Give search terms and hit enter)</small></li>
</ul>
<ul>
    <li style="width: 25em;">Powered by a homemade, experimental IRC logger written in Perl, PHP and MySQL.<br />
    A <a href="http://www.splitbrain.org">splitbrain.org</a> service.</li>
</ul>
</div></div>

</body>
</html>
<?php

/**
 * Callback to autolink a URL (with shortening)
 */
function format_link($match){
    $url = $match[1];
    $url = str_replace("\\\\'","'",$url);
    $link = '<a href="'.$url.'" rel="nofollow">'.$url.'</a>';
    return $link;
}


?>
