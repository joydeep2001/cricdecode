<?php
include_once "conn.php";
include_once "AppConsts.php";
include_once "GcMConsts.php";
set_time_limit ( 0 );
$id = $_POST ['id'];
$json_string = $_POST ['json'];
$json = str_replace ( "\\", "", $json_string );
$json_array = json_decode ( $json, true );
$order_id = $json_array ['orderId'];
$token = $json_array ['Token'];
$sign = $json_array ['Sign'];
$urlToSend = "https://accounts.google.com/o/oauth2/token";
$postdata = http_build_query ( array (
		'grant_type' => 'refresh_token',
		'client_id' => '899334271790-c2h5i9g18ann8b40hbv2pi497ubtu4ps.apps.googleusercontent.com',
		'client_secret' => 'qSaygF9EZZeeZM5rPGh6mKAM',
		'refresh_token' => '1/yBRC1qtHiAcnlGEEssoQe4ApgdA6zqu_1XPv4UI7LX8' 
) );
$opts = array (
		'http' => array (
				'method' => 'POST',
				'header' => 'Content-type: application/x-www-form-urlencoded',
				'content' => $postdata 
		) 
);
$context = stream_context_create ( $opts );
$result = file_get_contents ( $urlToSend, false, $context );
$rply = json_decode ( $result, true );
$url_to_send = "https://www.googleapis.com/androidpublisher/v1/applications/" . PACKAGE_NAME . "/subscriptions/" . PRODUCT_ID_sub_infi_sync . "/purchases/" . $token . "?access_token=" . $rply ['access_token'];
$tuCurl = curl_init ();
curl_setopt ( $tuCurl, CURLOPT_CONNECTTIMEOUT, 0 );
curl_setopt ( $tuCurl, CURLOPT_URL, $url_to_send );
curl_setopt ( $tuCurl, CURLOPT_RETURNTRANSFER, 1 );
curl_setopt ( $tuCurl, CURLOPT_HTTPGET, true );
curl_setopt ( $tuCurl, CURLOPT_HTTPHEADER, array (
		'Content-Type: application/json' 
) );
curl_setopt ( $tuCurl, CURLOPT_SSL_VERIFYPEER, false );
curl_setopt ( $tuCurl, CURLOPT_SSL_VERIFYHOST, 2 );
$tuData = curl_exec ( $tuCurl );
$reply = json_decode ( $tuData, true );
curl_close ( $tuCurl );
$init_ts1 = $reply ['initiationTimestampMsec'] * 0.001;
$init_ts = explode ( ".", $init_ts1 );
$valid_until_ts1 = $reply ['validUntilTimestampMsec'] * 0.001;
$valid_until_ts = explode ( ".", $valid_until_ts1 );
mysql_query ( "INSERT INTO sub_infi_sync VALUES('$id','$order_id','$token','$sign',$init_ts[0],$valid_until_ts[0])" );
$SendMsgArr = array (
		"gcmid" => SUB_INFI_SYNC 
);
SendGCm ( sendToArr ( $id ), $SendMsgArr, $id );
$ax = array (
		"status" => 1 
);
echo str_replace ( "\\", "", json_encode ( $ax ) );
?>