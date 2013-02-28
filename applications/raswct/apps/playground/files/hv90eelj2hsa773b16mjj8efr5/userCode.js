var led = new Rj.widget.Led("#led-widget");
setInterval(function(){
    var value = Math.random()*100 + Math.random();
    led.setValue(value);
}, 2000);