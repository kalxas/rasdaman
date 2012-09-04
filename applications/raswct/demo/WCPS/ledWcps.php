<?php
function do_post_request($url, $data, $optional_headers = null) {
    $params = array('http' => array(
            'method' => 'POST',
            'content' => $data
            ));
    if ($optional_headers !== null) {
        $params['http']['header'] = $optional_headers;
    }
    $ctx = stream_context_create($params);
    $fp = @fopen($url, 'rb', false, $ctx);
    if (!$fp) {
        throw new Exception("Problem with $url, $php_errormsg");
    }
    $response = @stream_get_contents($fp);
    if ($response === false) {
        throw new Exception("Problem reading data from $url, $php_errormsg");
    }
    return $response;
}


$url = 'http://212.201.49.173:8080/earthlook-ras';
$value = $_GET['value'];
$data = 'query=for t1 in (NN3_3) return encode (t1 [t('.$value.')], "csv")';


$result = do_post_request($url, $data);
$response = explode(",", str_replace(array("{", "}"), array("", ""), $result));
print json_encode($response);