<?python from genshi import HTML ?>

<script language="javascript">
  //<![CDATA[

function check() {
    box = document.getElementById('agree');
    if (box.checked)
	return true;
    else {
	alert("In order to upload the patch you should accept the contribution agreement");
	return false;
    }
}

function changeState() {
    box = document.getElementById('agreement');
    if (box.style.display != "inline")
	box.style.display = "inline";
    else
	box.style.display = "none";
    return false;
}

function decodePart(s) {
    var n = 0;
    var r = "";

    for( var i = 0; i < s.length; i++)
        {
            n = s.charCodeAt( i );
            if( n >= 8364 )
            {
                n = 128;
            }
            r += String.fromCharCode( n - 1 );
        }
    r = r.replace("<", "&lt;");
    r = r.replace(">", "&gt;");
    return r;
}

//]]>
</script>
