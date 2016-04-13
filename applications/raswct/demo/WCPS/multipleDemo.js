/**
 * Demonstration of diagram widgets
 * @author Alex Dumitru <m.dumitru@jacobs-university.de>
 * @author Vlad Merticariu <v.merticariu@jacobs-university.de>
 * @version 1.0.0
 */
CoverageDiagramDisplay = function(dType, selector){
  this.diagramType = dType;
  this.initiated = false;
  console.log(Rj.Widget)
  this.diagram = new Rj.Widget[this.diagramType]("Diagram Example", "XAxis", "YAxis", ["#000", "#ABC", "#DEF", "#777", "#555"]);
  this.coverageIndexes = {};
  this.selector = selector;  
  this.series = [];
}

CoverageDiagramDisplay.prototype.getCoverages = function(){
  var covs = []
  for(var index in this.coverageIndexes){
    covs.push(index);
  }
  return covs;
}

CoverageDiagramDisplay.prototype.addCoverage = function(coverageId){
  var query = new Rj.Query.UrlQuery("wcpsParser.php", Rj.Constants.UrlQuery.GET, {
    coverageId : coverageId
  });
  var self = this;
  var exec = new Rj.Executor.QueryExecutor(query);
  exec.callback(function(response){
    var diagramData = [] 
    var ticks = [];
    var processedResp = JSON.parse(response);
    for(var i = 0; i < processedResp.data.length; i++){
      diagramData.push([i, parseInt(processedResp.data[i], 10)]);
      ticks.push(i.toString());
    }
    if(self.diagramType == "BarChart"){
      self.diagram.setTicks(ticks);
    }
    var index = self.diagram.addDataSeries(diagramData, coverageId);
    self.coverageIndexes[coverageId] = index;
    if(!self.initiated){
      self.createChart();
      self.initiated = true;
    }
  })
};

CoverageDiagramDisplay.prototype.changeType = function(dType){
  $(this.selector).html("");
  var coverages = this.getCoverages();
  this.diagramType = dType;
  this.diagram = new Rj.Widget[dType];
  this.initiated = false;
  for(var i = 0; i < coverages.length; i++){
    this.addCoverage(coverages[i]);
  }
  
}

CoverageDiagramDisplay.prototype.removeCoverage = function(coverageId){
  this.diagram.removeDataSeries(this.coverageIndexes[coverageId], false);
  delete this.coverageIndexes[coverageId];
};

CoverageDiagramDisplay.prototype.createChart = function(){
  this.diagram.renderTo(this.selector);    
};

(function($){
    
  $(document).ready(function(){
    var covDisp = new CoverageDiagramDisplay("LinearDiagram", "#chartPlace");
    covDisp.addCoverage("NN3_1");
    covDisp.addCoverage("NN3_2");    
    $(".nns").click(function(){
      var selected = $(this).attr('checked') ? true : false;
      if(selected){
        covDisp.addCoverage($(this).val());        
      }
      else{
        covDisp.removeCoverage($(this).val());
      }
    });  
    
    $("#change-diagram").change(function(){
      var val = $(this).val();
      covDisp.changeType(val);      
    })
        
  })
    
})(jQuery);
