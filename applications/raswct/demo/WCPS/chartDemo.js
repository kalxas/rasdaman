/**
 * Demonstration of diagram widgets
 * @author Alex Dumitru <m.dumitru@jacobs-university.de>
 * @author Vlad Merticariu <v.merticariu@jacobs-university.de>
 * @version 1.0.0
 */

(function($){
    $(document).ready(function(){
        var diagram = new Rj.Widget.LinearDiagram("NN3", "t", "Values");
        var changeDataSource = function(coverageId){
            var query = new Rj.Query.UrlQuery('wcpsParser.php', 'GET', {
                coverageId : coverageId
            });
            var exec = new Rj.Executor.QueryExecutor(query);
            exec.callback(function(response){
                var diagramData = [] 
                var processedResp = JSON.parse(response);
                for(var i = 0; i < processedResp.data.length; i++){
                    diagramData.push([i, parseInt(processedResp.data[i], 10)]);
                }
                diagram.addDataSeries(diagramData);
                diagram.renderTo("#chartPlace");
            })            
        }
        $("#chart-source").keypress(function(event){
            if(event.keyCode == 13){
                diagram.removeDataSeries(0, true);
                changeDataSource($(this).val());
            }
        });
        
        $("#change-source").click(function(event){
            diagram.removeDataSeries(0, true);
            changeDataSource($("#chart-source").val());
        });
        changeDataSource("NN3_10");
    })
})(jQuery);
