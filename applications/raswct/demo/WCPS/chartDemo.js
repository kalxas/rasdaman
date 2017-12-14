/**
 * Demonstration of diagram widgets
 * @author Alex Dumitru <m.dumitru@jacobs-university.de>
 * @author Vlad Merticariu <v.merticariu@jacobs-university.de>
 * @version 1.0.0
 */

(function ($) {
  $(document).ready(function () {
    var diagram = new Rj.Widget.LinearDiagram("NN3", "t", "Values");
    var changeDataSource = function(val){
      var query = new Rj.Query.UrlQuery("wcpsParser.php", "POST", {coverageId : val});
      query.evaluate(function(response){
        console.log(response);
        diagram.addDataSeries(response,"name");
        diagram.renderTo("#chartPlace");
      })
    }
    $("#chart-source").keypress(function (event) {
      if (event.keyCode == 13) {
        changeDataSource($(this).val());
      }
    });

    $("#change-source").click(function (event) {
      changeDataSource($("#chart-source").val());
    });
    changeDataSource("NN3_10");

    var slider = new Rj.Widget.VerticalSlider(1, 10, 1, 1);
    slider.renderTo("#slider");
    slider.onSlide(function(value){console.log("Slide:" +value.toString())})
    slider.onChange(function(value){console.log("Change:" +value.toString())})
    //setTimeout(function(){ console.log("dest"); slider.refresh();}, 6000);
  })

})(jQuery);
