$(document).ready(function(){
  var led = new Rj.Widget.LedDisplay(null,0);
  led.setValue(2478);
  var gauge = new Rj.Widget.Gauge(null,24);
  var tGauge = new Rj.Widget.Gauge(null, 2478, '', true);
  var jGauge = new Rj.Widget.JGauge(null, 'Exact value', '', 0, 10000, true, 2478, 1, true, '#ccc');
  var jGauge2 = new Rj.Widget.JGauge(null, 'Trimmed value', '', 0, 100, true, 24);

  led.renderTo("#led");
  gauge.renderTo("gauge");
  tGauge.renderTo("gaugeTaco");
  jGauge.renderTo("jgauge");
  jGauge2.renderTo("jgauge2");

  var setValues = function(){
    var floatRand = Math.random();
    var rand = parseInt(floatRand*100);
    var query = new Rj.Query.UrlQuery('ledWcps.php', 'GET', {
      'value': rand
    });
    var exec = new Rj.Executor.QueryExecutor(query);
    exec.callback(function(response){
      var data = JSON.parse(response);
      var no = data[0];
      $("#query").text('for t1 in (NN3_3)\n return\n encode (t1 [t(' + rand + ')], "csv")');
      led.setValue(no);
      gauge.setValue(parseInt(no/100));
      tGauge.setValue(no);
      jGauge.setValue(no);
      jGauge2.setValue(parseInt(no/100))
    });
  }
    
  var interval = setInterval(function(){
    setValues();
  }, 8000);
    
  var knob = new Rj.Widget.Knob(5, 10, 8);
  knob.renderTo("knob");
  knob.addListener('knob','knobchange', function(value){
    $("#sec").html(value);
    clearInterval(interval);
    interval = setInterval(function(){
      setValues()
    }, value*1000);
  })
})