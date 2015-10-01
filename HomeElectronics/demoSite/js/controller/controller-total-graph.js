/**
 controller-total-graph.js
 Copyright (c) 2015 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */

(function() {

  function controller($scope, powermeter){

    var container = $("#placeholder");
    var maximum = 15;
    var series;
    var plot;

    init();

    function init(){
      setValuesToScope();
      setupPowerPolling();
      setupGraph();
    }

    function setValuesToScope(){
      $scope.nav = {
        goBack: true,
        goGraph: false,
        goSetting: false,
        title: '総消費電力量グラフ'
      };
      $scope.isTotal = true;
      $scope.consumption = getDefaultConsumption();
    }

    function getDefaultConsumption(){
      var consumption = powermeter.getLatestValue();
      if(consumption){
        consumption = consumption.toFixed(2);
      }
      return consumption || '...';
    }

    function setupPowerPolling(){
      powermeter.setPollingListener(function(results){
        var total = 0;
        angular.forEach(results,function(result){
          total += result.value;
        });
        $scope.$apply(function(){
          $scope.consumption = total.toFixed(2);
        });
      });
    }

    function setupGraph(){
      series = [{
          data:getLatestData(),
          lines:{fill:false}
        }];
      plot = $.plot(container, series, {
        grid:{
          borderWidth:1, minBorderMargin:20, labelMargin:10,
          backgroundColor:{ colors:["#fff","#e4f4f4"] },
          margin:{ top:8, bottom:20, left:20 },
          markings:function(axes){
            var markings = [];
            var xaxis = axes.xaxis;
            for(var x = Math.floor(xaxis.min);x < xaxis.max;x+= xaxis.tickSize * 2){
              markings.push({
                xaxis:{from:x,to:x+xaxis.tickSize},
                color:"rgba(232,232,255,0.2)"
              });
            }
            return markings;
          }
        },
        xaxis:{ tickFormatter:function(){ return ""; } },
        yaxis:{ min:0, max:50},
        legend:{ show:true }
      });
    }

    function getLatestData() {
      var data = powermeter.getData();
      var limit = maximum - data.length;
      var res = [];
      var index = 0;
      for(var i = 0; i < maximum; i++){
        if(i < limit){
          res.push([i,0]);
        } else {
          res.push([i,data[index++]]);
        }
      }
      return res;
    }

    function updateData(){
      series[0].data = getLatestData();
      plot.setData(series);
      plot.draw();
    }
  }

  angular.module('HomeDemo').controller('TotalGraphController',
  ['$scope', 'powermeter', controller]);
})();
