declare namespace gml = "http://www.opengis.net/gml/3.2";
declare namespace xlink = "http://www.w3.org/1999/xlink";
declare function local:getid($d as document-node(), $id as xs:string) as element() {
	let $ret := $d//gml:identifier[contains(text(), $id)]/..
	return  if (empty($ret)) then
	        <empty/>
	       else
		$ret[last()]
};
declare function local:flatten($d as document-node(), $id as xs:string, $depth as xs:integer) as element()* {
  copy $el := local:getid($d, $id)
  modify
  (
  for $c in $el/*[@xlink:href]
  return if ($depth < 2) then
	replace node $c with local:flatten($d, $c/@xlink:href, $depth + 1)
	  else replace node $c with $c
  )
  return $el
};
declare function local:work($id as xs:string) as element() {
	let $res := local:flatten(collection()[1], $id, 0)
  return
    copy $tmp := $res
    modify (
      if (exists($tmp//gml:greenwichLongitude)) then replace value of node $tmp//gml:greenwichLongitude with '-99.0' else {}
    )
    return $tmp
};
local:work('/crs/EPSG/0/4326')
