/**
 service-powermeter.js
 Copyright (c) 2015 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */

(function() {

  var DATA_LIMIT = 15;
  var client;
  var list = [];
  var pollings = [];
  var valueData = [];
  var devicesService;
  var lightService;
  var pollingListener;

  function powermeter(devices, light, manager){
    devicesService = devices;
    lightService = light;
    client = manager.getClient();

    setPollingListener(function(results){});
    setTimeout(function(){
      polling();
    }, 5000);
    devices.setPowermeterListener(function(list){
      pollings = list;
    });

    this.setPollingListener = setPollingListener;
    this.getPowerMeters = getPowerMeters;
    this.getData = getData;
    this.getLatestValue = getLatestValue;
    this.lightOn = lightOn;
    this.lightOff = lightOff;
  }

  function setPollingListener(listener){
    pollingListener = listener;
  }

  function getPowerMeters(callback){
    devicesService.getDevices(function(result){
      if(result.isSuccess){
        list = [];
        angular.forEach(result.devices, function(device){
          if(device.powermeter){
            list.push({id:device.id, name:device.name});
          }
        });
        callback({isSuccess:true, devices:list});
      } else {
        callback({isSuccess:false, error:result.error});
      }
    });
  }

  function getData(serviceId){
    var data = valueData;
    var list = [];
    angular.forEach(data, function(section){
      var value = 0;
      angular.forEach(section, function(item){
        if(serviceId){
          if(serviceId === item.serviceId){
            value = item.value;
          }
        } else {
          value += item.value;
        }
      });
      list.push(value);
    });
    return list;
  }

  function getLatestValue(serviceId){
    var data = valueData;
    if(data.length === 0){
      return undefined;
    }
    var latestSection = data[data.length - 1];
    var latestValue;
    angular.forEach(latestSection, function(item){
      if(serviceId){
        if(serviceId === item.serviceId){
          latestValue = item.value;
        }
      } else {
        if(!latestValue){
          latestValue = 0;
        }
        latestValue += item.value;
      }
    });
    return latestValue;
  }

  ///F-PLUG(Powermeter profile及びLight profileに対応し、ServiceIdのみでライトが特定できる端末)専用の特別処理。
  //電力計デバイスのライトのON/OFFを行う。
  function lightOn(serviceId, callback){
    lightService.resetStatus();
    lightService.fetchLight(serviceId, function(result){
      if(result.isSuccess){
        lightService.setServiceId(serviceId);
        lightService.setLightId(result.lights[0].lightId);
        lightService.lightOn(callback);
      } else {
        callback(result);
      }
    });
  }

  function lightOff(serviceId, callback){
    lightService.resetStatus();
    lightService.fetchLight(serviceId, function(result){
      if(result.isSuccess){
        lightService.setServiceId(serviceId);
        lightService.setLightId(result.lights[0].lightId);
        lightService.lightOff(callback);
      } else {
        callback(result);
      }
    });
  }

  function polling(){
    deferredObjects = [];
    var list = pollings;
    angular.forEach(list, function(powermeterId){
      deferredObjects.push(getInstantaneousPowerValues(powermeterId));
    });
    $.when.apply(null, deferredObjects).done(function() {
      if(arguments && arguments.length){
        saveData(arguments);
        pollingListener(arguments);
      }
    });
    setTimeout(polling,5000);
  }

  function saveData(data){
    if(valueData.length === DATA_LIMIT){
      valueData.shift();
    }
    valueData.push(data);
  }

  function getInstantaneousPowerValues(serviceId){
    var dfd = $.Deferred();
    getInstantaneousPowerValue(serviceId, function(result){
      if(result.isSuccess){
        dfd.resolve({serviceId:serviceId, value:result.value});
      } else {
        dfd.resolve();
      }
    });
    return dfd.promise();
  }

  function getInstantaneousPowerValue(serviceId, callback){
    var reqObj = {
      serviceId:serviceId,
      method:'GET',
      profile:'powermeter',
      attribute: 'instantaneouspowervalue',
      onresult: function(result){
        if(result.isSuccess){
          callback({isSuccess:true, value:result.response.instantaneouspowervalue});
        } else {
          callback({isSuccess:false, error:result.error});
        }
      }
    };
    client.request(reqObj);
  }

  angular.module('HomeDemo').service('powermeter', ['devices', 'light', 'manager', powermeter]);
})();
