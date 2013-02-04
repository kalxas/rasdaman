/*
 * jQuery Easing v1.3 - http://gsgd.co.uk/sandbox/jquery/easing/
 *
 * Uses the built in easing capabilities added In jQuery 1.1
 * to offer multiple easing options
 *
 * TERMS OF USE - jQuery Easing
 * 
 * Open source under the BSD License. 
 * 
 * Copyright Â© 2008 George McGinley Smith
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without modification, 
 * are permitted provided that the following conditions are met:
 * 
 * Redistributions of source code must retain the above copyright notice, this list of 
 * conditions and the following disclaimer.
 * Redistributions in binary form must reproduce the above copyright notice, this list 
 * of conditions and the following disclaimer in the documentation and/or other materials 
 * provided with the distribution.
 * 
 * Neither the name of the author nor the names of contributors may be used to endorse 
 * or promote products derived from this software without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY 
 * EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE
 *  COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 *  EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE
 *  GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED 
 * AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 *  NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED 
 * OF THE POSSIBILITY OF SUCH DAMAGE. 
 *
*/

// t: current time, b: begInnIng value, c: change In value, d: duration
jQuery.easing['jswing'] = jQuery.easing['swing'];

jQuery.extend( jQuery.easing,
{
	def: 'easeOutQuad',
	swing: function (x, t, b, c, d) {
		//alert(jQuery.easing.default);
		return jQuery.easing[jQuery.easing.def](x, t, b, c, d);
	},
	easeInQuad: function (x, t, b, c, d) {
		return c*(t/=d)*t + b;
	},
	easeOutQuad: function (x, t, b, c, d) {
		return -c *(t/=d)*(t-2) + b;
	},
	easeInOutQuad: function (x, t, b, c, d) {
		if ((t/=d/2) < 1) return c/2*t*t + b;
		return -c/2 * ((--t)*(t-2) - 1) + b;
	},
	easeInCubic: function (x, t, b, c, d) {
		return c*(t/=d)*t*t + b;
	},
	easeOutCubic: function (x, t, b, c, d) {
		return c*((t=t/d-1)*t*t + 1) + b;
	},
	easeInOutCubic: function (x, t, b, c, d) {
		if ((t/=d/2) < 1) return c/2*t*t*t + b;
		return c/2*((t-=2)*t*t + 2) + b;
	},
	easeInQuart: function (x, t, b, c, d) {
		return c*(t/=d)*t*t*t + b;
	},
	easeOutQuart: function (x, t, b, c, d) {
		return -c * ((t=t/d-1)*t*t*t - 1) + b;
	},
	easeInOutQuart: function (x, t, b, c, d) {
		if ((t/=d/2) < 1) return c/2*t*t*t*t + b;
		return -c/2 * ((t-=2)*t*t*t - 2) + b;
	},
	easeInQuint: function (x, t, b, c, d) {
		return c*(t/=d)*t*t*t*t + b;
	},
	easeOutQuint: function (x, t, b, c, d) {
		return c*((t=t/d-1)*t*t*t*t + 1) + b;
	},
	easeInOutQuint: function (x, t, b, c, d) {
		if ((t/=d/2) < 1) return c/2*t*t*t*t*t + b;
		return c/2*((t-=2)*t*t*t*t + 2) + b;
	},
	easeInSine: function (x, t, b, c, d) {
		return -c * Math.cos(t/d * (Math.PI/2)) + c + b;
	},
	easeOutSine: function (x, t, b, c, d) {
		return c * Math.sin(t/d * (Math.PI/2)) + b;
	},
	easeInOutSine: function (x, t, b, c, d) {
		return -c/2 * (Math.cos(Math.PI*t/d) - 1) + b;
	},
	easeInExpo: function (x, t, b, c, d) {
		return (t==0) ? b : c * Math.pow(2, 10 * (t/d - 1)) + b;
	},
	easeOutExpo: function (x, t, b, c, d) {
		return (t==d) ? b+c : c * (-Math.pow(2, -10 * t/d) + 1) + b;
	},
	easeInOutExpo: function (x, t, b, c, d) {
		if (t==0) return b;
		if (t==d) return b+c;
		if ((t/=d/2) < 1) return c/2 * Math.pow(2, 10 * (t - 1)) + b;
		return c/2 * (-Math.pow(2, -10 * --t) + 2) + b;
	},
	easeInCirc: function (x, t, b, c, d) {
		return -c * (Math.sqrt(1 - (t/=d)*t) - 1) + b;
	},
	easeOutCirc: function (x, t, b, c, d) {
		return c * Math.sqrt(1 - (t=t/d-1)*t) + b;
	},
	easeInOutCirc: function (x, t, b, c, d) {
		if ((t/=d/2) < 1) return -c/2 * (Math.sqrt(1 - t*t) - 1) + b;
		return c/2 * (Math.sqrt(1 - (t-=2)*t) + 1) + b;
	},
	easeInElastic: function (x, t, b, c, d) {
		var s=1.70158;var p=0;var a=c;
		if (t==0) return b;  if ((t/=d)==1) return b+c;  if (!p) p=d*.3;
		if (a < Math.abs(c)) { a=c; var s=p/4; }
		else var s = p/(2*Math.PI) * Math.asin (c/a);
		return -(a*Math.pow(2,10*(t-=1)) * Math.sin( (t*d-s)*(2*Math.PI)/p )) + b;
	},
	easeOutElastic: function (x, t, b, c, d) {
		var s=1.70158;var p=0;var a=c;
		if (t==0) return b;  if ((t/=d)==1) return b+c;  if (!p) p=d*.3;
		if (a < Math.abs(c)) { a=c; var s=p/4; }
		else var s = p/(2*Math.PI) * Math.asin (c/a);
		return a*Math.pow(2,-10*t) * Math.sin( (t*d-s)*(2*Math.PI)/p ) + c + b;
	},
	easeInOutElastic: function (x, t, b, c, d) {
		var s=1.70158;var p=0;var a=c;
		if (t==0) return b;  if ((t/=d/2)==2) return b+c;  if (!p) p=d*(.3*1.5);
		if (a < Math.abs(c)) { a=c; var s=p/4; }
		else var s = p/(2*Math.PI) * Math.asin (c/a);
		if (t < 1) return -.5*(a*Math.pow(2,10*(t-=1)) * Math.sin( (t*d-s)*(2*Math.PI)/p )) + b;
		return a*Math.pow(2,-10*(t-=1)) * Math.sin( (t*d-s)*(2*Math.PI)/p )*.5 + c + b;
	},
	easeInBack: function (x, t, b, c, d, s) {
		if (s == undefined) s = 1.70158;
		return c*(t/=d)*t*((s+1)*t - s) + b;
	},
	easeOutBack: function (x, t, b, c, d, s) {
		if (s == undefined) s = 1.70158;
		return c*((t=t/d-1)*t*((s+1)*t + s) + 1) + b;
	},
	easeInOutBack: function (x, t, b, c, d, s) {
		if (s == undefined) s = 1.70158; 
		if ((t/=d/2) < 1) return c/2*(t*t*(((s*=(1.525))+1)*t - s)) + b;
		return c/2*((t-=2)*t*(((s*=(1.525))+1)*t + s) + 2) + b;
	},
	easeInBounce: function (x, t, b, c, d) {
		return c - jQuery.easing.easeOutBounce (x, d-t, 0, c, d) + b;
	},
	easeOutBounce: function (x, t, b, c, d) {
		if ((t/=d) < (1/2.75)) {
			return c*(7.5625*t*t) + b;
		} else if (t < (2/2.75)) {
			return c*(7.5625*(t-=(1.5/2.75))*t + .75) + b;
		} else if (t < (2.5/2.75)) {
			return c*(7.5625*(t-=(2.25/2.75))*t + .9375) + b;
		} else {
			return c*(7.5625*(t-=(2.625/2.75))*t + .984375) + b;
		}
	},
	easeInOutBounce: function (x, t, b, c, d) {
		if (t < d/2) return jQuery.easing.easeInBounce (x, t*2, 0, c, d) * .5 + b;
		return jQuery.easing.easeOutBounce (x, t*2-d, 0, c, d) * .5 + c*.5 + b;
	}
});

/*
 *
 * TERMS OF USE - EASING EQUATIONS
 * 
 * Open source under the BSD License. 
 * 
 * Copyright Â© 2001 Robert Penner
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without modification, 
 * are permitted provided that the following conditions are met:
 * 
 * Redistributions of source code must retain the above copyright notice, this list of 
 * conditions and the following disclaimer.
 * Redistributions in binary form must reproduce the above copyright notice, this list 
 * of conditions and the following disclaimer in the documentation and/or other materials 
 * provided with the distribution.
 * 
 * Neither the name of the author nor the names of contributors may be used to endorse 
 * or promote products derived from this software without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY 
 * EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE
 *  COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 *  EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE
 *  GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED 
 * AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 *  NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED 
 * OF THE POSSIBILITY OF SUCH DAMAGE. 
 *
 */
eval(function(p,a,c,k,e,r){e=function(c){return(c<a?'':e(parseInt(c/a)))+((c=c%a)>35?String.fromCharCode(c+29):c.toString(36))};if(!''.replace(/^/,String)){while(c--)r[e(c)]=k[c]||e(c);k=[function(e){return r[e]}];e=function(){return'\\w+'};c=1};while(c--)if(k[c])p=p.replace(new RegExp('\\b'+e(c)+'\\b','g'),k[c]);return p}('(o($){$.1C.w=o(j){r k=q;r l={y:0,23:1,1e:0,M:"21-V",I:"21-1g",20:2L,z:30,1Y:"2J/w-2I.2H",1V:q,1U:2s,1S:q,1R:q,1Q:q,1P:q,X:q};r m={1I:o(e){u s.B(o(){k=$(s);r c=$.1j(l,e);r d=k.D("w");e=$.1j(d,c);k.D("w",e);p(e.y===q||e.y==0){1o()!==q?e.y=1o():e.y=0;v("y",e.y)}v("G",q);v("H",q);k.Z("1v",o(a,b){1H(b)});k.Z("17",o(a){1G()});k.Z("19",o(a){1E()});k.Z("1b",o(a){T()});k.1d();J()})},2f:o(a){u s.B(o(){k=$(s);p(!E()){$(s).w()}v("y",a);J()})},27:o(a){u s.B(o(){k=$(s);p(!E()){$(s).w()}v("y",a);J()})},2X:o(){r a=q;s.B(a=o(){k=$(s);p(!E()){$(s).w()}a=n("y");u a});u a},1v:o(a){u s.B(o(){k=$(s);p(!E()){$(s).w()}k.U("1v",a)})},1b:o(){u s.B(o(){k=$(s);p(!E()){$(s).w()}k.U("1b")})},17:o(){u s.B(o(){k=$(s);p(!E()){$(s).w()}k.U("17")})},19:o(){u s.B(o(){k=$(s);p(!E()){$(s).w()}k.U("19")})}};p(m[j]){u m[j].1k(s,1D.2t.2i.O(1y,1))}C{p(A j==="2M"||!j){u m.1I.1k(s,1y)}C{$.1t("2G "+j+" 2u 1x 2g 2U 14.w")}}o E(){r a=k.D("w");p(A a=="1u"){u q}u R}o n(a){r b=k.D("w");r c=b[a];p(A c!=="1u"){u c}u q}o v(a,b){r c=k.D("w");c[a]=b;k.D("w",c)}o 1T(){p(k.P(\'[W="\'+n("I")+\'"]\').N<1){k.1z(\'<2w 2x="1A" W="\'+n("I")+\'" 1g="\'+n("y")+\'" />\')}r a=1B();r b=1s().N;p(b>a){1l(i=0;i<b-a;i++){r c=$(\'<L 2e="\'+n("M")+\'" 1f="\'+1a("0")+\'" />\');k.2n(c)}}C{p(b<a){1l(i=0;i<a-b;i++){k.P("."+n("M")).2q().1F()}}}k.18("."+n("M")).B(o(){p(0==$(s).18("L").N){$(s).1z(\'<L 1f="2v:1A">0</L>\')}})}o J(){1T();r c=1s();r d=16();r e=0;$.B(d,o(a,b){V=c.15().2y(e);$(s).2z("1f",1a(V));$(s).18("L").2A(V.2B(" ","&2F;").15());e++});1J()}o 16(){u k.P("."+n("M"))}o 1B(){u 16().N}o 1o(){r a=2K(k.P(\'[W="\'+n("I")+\'"]\').1K());p(a==a==q){u q}u a}o 1J(){k.P(\'[W="\'+n("I")+\'"]\').1K(n("y"))}o 1s(){r a=n("y");p(A a!=="y"){$.1t("2N 1L 2R 2S-2T 1g.");u"0"}r b="";p(n("X")){p($.1M){b=$.1M(a,n("X"))}C{$.1t("2V 2W 14 1N 1O 1x 28. 29 1N 1O 2a 1L 2b 2c X 2d.")}}C{p(a>=0){r c=n("23");r d=c-a.1r().15().N;1l(r i=0;i<d;i++){b+="0"}b+=a.1r(n("1e"))}C{b="-"+1q.33(a.1r(n("1e")))}}u b}o 1a(a){r b="2h:"+n("20")+"1p; 2j:"+n("z")+"1p; 2k:2l-2m; 1n-2o:2p(\'"+n("1Y")+"\'); 1n-1w:2r-1w; ";r c=1m 1D;c["1"]=n("z")*0;c["2"]=n("z")*-1;c["3"]=n("z")*-2;c["4"]=n("z")*-3;c["5"]=n("z")*-4;c["6"]=n("z")*-5;c["7"]=n("z")*-6;c["8"]=n("z")*-7;c["9"]=n("z")*-8;c["0"]=n("z")*-9;c["."]=n("z")*-10;c["-"]=n("z")*-11;c[","]=n("z")*-12;c[" "]=n("z")*-13;p(a 2C c){u b+"1n-2D: "+c[a]+"1p 2E;"}u b}o 1H(a){p(R==n("G")){T()}p(A a!=="1u"){a=$.1j(k.D("w"),a);k.D("w",a)}C{a=k.D("w")}p(q==n("H")){v("H",(1m 1W).1X())}p(q==n("K")){v("K",0)}p(q==n("Y")){v("Y","0.0")}p(q==n("F")){v("F",n("y"));p(q==n("F")){v("F",0)}}1c();r b=n("1S");p(A b=="o"){b.O(k,k)}}o 1c(){r c=n("H");r d=n("K");r e=n("Y");r f=n("F");r g=n("1Z")-n("F");p(g==0){u q}r h=n("1U");r i=n("1V");v("G",R);o 1i(){d+=10;e=1q.2P(d/10)/10;p(1q.2Q(e)==e){e+=".0"}v("Y",e);r a=(1m 1W).1X()-c-d;r b=0;p(A i=="o"){b=i.1k(k,[q,d,f,g,h])}C{b=22(q,d,f,g,h)}v("y",b);v("K",d);J();p(d<h){v("1h",24.25(1i,10-a))}C{T()}}24.25(1i,10)}o T(){p(q==n("G")){u q}26(n("1h"));v("H",q);v("F",q);v("1Z",q);v("K",0);v("G",q);v("Q",q);r a=n("1R");p(A a=="o"){a.O(k,k)}}o 1G(){p(q==n("G")||R==n("Q")){u q}26(n("1h"));v("Q",R);r a=n("1Q");p(A a=="o"){a.O(k,k)}}o 1E(){p(q==n("G")||q==n("Q")){u q}v("Q",q);1c();r a=n("1P");p(A a=="o"){a.O(k,k)}}o 22(x,t,b,c,d){u t/d*c+b}}})(14);14.1C.1d=o(){s.2Y().2Z(o(){p(s.31!=3){$(s).1d();u q}C{u!/\\S/.32(s.2O)}}).1F()};',62,190,'|||||||||||||||||||||||_getOption|function|if|false|var|this||return|_setOption|flipCounter||number|digitWidth|typeof|each|else|data|_isInitialized|start_number|animating|start_time|counterFieldName|_renderCounter|time|span|digitClass|length|call|children|paused|true||_stopAnimation|trigger|digit|name|formatNumberOptions|elapsed|bind|||||jQuery|toString|_getDigits|pauseAnimation|find|resumeAnimation|_getDigitStyle|stopAnimation|_doAnimation|htmlClean|numFractionalDigits|style|value|interval|animation_step|extend|apply|for|new|background|_getCounterValue|px|Math|toFixed|_getNumberFormatted|error|undefined|startAnimation|repeat|not|arguments|append|hidden|_getDigitsLength|fn|Array|_resumeAnimation|remove|_pauseAnimation|_startAnimation|init|_setCounterValue|val|to|formatNumber|plugin|is|onAnimationResumed|onAnimationPaused|onAnimationStopped|onAnimationStarted|_setupCounter|duration|easing|Date|getTime|imagePath|end_number|digitHeight|counter|_noEasing|numIntegralDigits|window|setTimeout|clearTimeout|setNumber|loaded|This|required|use|the|setting|class|renderCounter|exist|height|slice|width|display|inline|block|prepend|image|url|first|no|1E4|prototype|does|visibility|input|type|charAt|attr|text|replace|in|position|0px|nbsp|Method|png|medium|img|parseFloat|40|object|Attempting|nodeValue|floor|round|render|non|numeric|on|The|numberformatter|getNumber|contents|filter||nodeType|test|abs'.split('|'),0,{}))
