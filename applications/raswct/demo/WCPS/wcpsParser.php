<?php

/**

 * Gets information about a WCPS coverage and returns it in a JSON encode of an array,
 * which can be easily accessed through javascript.
 * 
 * The parameter it needs is the coverage id, which is passed through GET['coverageId']
 * 
 * @author Mircea Dumitru <m.dumitru@jacobs-university.de>
 * @author Vlad Merticariu <v.merticariu@jacobs-university.de>
 * @version 1.0
 */
function get_data($url) {
  $ch = curl_init();
  $timeout = 5;
  curl_setopt($ch, CURLOPT_URL, $url);
  curl_setopt($ch, CURLOPT_RETURNTRANSFER, 1);
  curl_setopt($ch, CURLOPT_CONNECTTIMEOUT, $timeout);
  $data = curl_exec($ch);
  curl_close($ch);
  return $data;
}


$coverageId = $_GET['coverageId'];
$coverageRaw = get_data('http://kahlua.eecs.jacobs-university.de:8080/petascope?service=WCS&version=2.0.0&request=GetCoverage&coverageId=' . $coverageId);
if (!strstr($coverageRaw, '<ows:ExceptionText>')) {
    $coverage = simplexml_load_string($coverageRaw);
    $lowDomainLimit = (string) $coverage->domainSet->Grid->limits->GridEnvelope->low;
    $highDomainLimit = (string) $coverage->domainSet->Grid->limits->GridEnvelope->high;
    $domainAxisLabel = (string) $coverage->domainSet->Grid->axisLabels;

    $values = explode(',', str_replace(array('{', '}'), array('',''), (string) $coverage->rangeSet->DataBlock->tupleList));

    $results = array(
        'domainInfo' => array(
            'lowLimit' => $lowDomainLimit,
            'highLimit' => $highDomainLimit,
            'axisLabel' => $domainAxisLabel
        ),
        'data' => $values
    );
}
else{
    $errorRaw = explode('<ows:ExceptionText>', $coverageRaw);
    $error = explode('</ows:ExceptionText>', $errorRaw[1]);
    $results = array(
      'error' => trim($error[0])
    );
}

print json_encode($results);





