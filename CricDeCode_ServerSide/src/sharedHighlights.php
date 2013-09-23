<html>
<head>
<link rel="stylesheet" type="text/css" href="main.css">
</head>
<?php
$fn = $_GET ['fn'];
$ln = $_GET ['ln'];
$ta = $_GET ['ta'];
$tb = $_GET ['tb'];
$v = $_GET ['v'];
$lvl = $_GET ['lvl'];
$bat = $_GET ['bat'];
$bowl = $_GET ['bowl'];
$field = $_GET ['field'];
?>
<div id="topbar"></div>
<img id="logo" src="icon.png" />
<div id="appname">CricDeCode</div>
<a id="getApp" href="www.google.com">Go to PlayStore</a>
<div id="info_l">
	<div id="info_l_top">
<? echo $fn.'<br />'; ?>
<? echo '<b>'.$ln.'</b><br />'; ?>
<hr id="rule" />
	</div>
	<div id="info_l_mid">
<? echo $ta.'<br />'; ?>
<div style="font-size: 0.7em;">vs</div>
<? echo $tb.'<br />'; ?>
<div style="font-size: 0.7em; margin-top: 2px;"><? echo $v.'<br />'; ?></div>
<div style="font-size: 0.7em; padding-top: 10px;">
<? echo $lvl." Level"; ?>
</div>
	</div>
	
</div>
<div id="info_r1">
	Batting Performance
	<hr id="rule" />
<div id="per">

<?	
if($bat=="")
{
echo "No data available";
}
else
{
 echo $bat;
} ?>
</div>
</div>
<div id="info_r2">
	Bowling Performance
	<hr id="rule" />
<div id="per">
<?	
if($bowl=="")
{
 echo "No data available"; 
}
else
{
echo $bowl;
} ?>
</div>
</div>
<div id="info_r3">
	Fielding Performance
	<hr id="rule" />
	<div id="per">

<?
if($field=="")
{
 echo "No data available"; 
}
else
{
 echo $field; 
}?>
</div>
</div>
<div id="bottomCard">
CricDeCode provides a personal, local and easy to maintain data store with intuitive statistics generation. CricDeCode requires the cricketer to feed his match performances. CricDeCode then provides speedy access to detailed career and statistics generation and intuitive graphical views, enabling effective personal performance profiling. Your data is secured with cloud backup and inter device data syncing.
CricDeCode also provides a platform to post the highlights of your performances on Facebook.</div>
</html>