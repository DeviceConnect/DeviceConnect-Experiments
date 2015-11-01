/**
 service-devices.js
 Copyright (c) 2015 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */

(function() {

  var managerService;
  var storeService;
  var lightService;

  var list = [];
  var callbacks = [];
  var requesting = false;
  var powermeterListener;

  function devices(manager, store, light){
    managerService = manager;
    storeService = store;
    lightService = light;

    this.getDeviceName = getDeviceName;
    this.getDevices = getDevices;
    this.getSelectedPowerMeterId = getSelectedPowerMeterId;
    this.setPowermeterListener = setPowermeterListener;
    this.setPowerMeter = setPowerMeter;
  }

  function getDeviceName(deviceId){
    var name;
    angular.forEach(list, function(device){
      if(device.id === deviceId){
        name = device.name;
      }
    });
    if(!name){
      name = storeService.getDeviceName(deviceId);
      if(!name){
        name = '不明';
      }
    }
    return name;
  }

  function getDevices(callback){
    callbacks.push(callback);
    if(requesting){
      return;
    }
    if(list.length > 0){
      setTimeout(function(){
        refreshName(list);
        doCallback({isSuccess:true,devices:list});
      }, 0);
      return;
    }
    requesting = true;
    managerService.discovery({
      onsuccess:function(devices){
        list = [];
        lightList = [];
        makeDeviceList(devices, list, lightList);
        if(lightList.length > 0){
          makeLightDeviceList(lightList, function(resultList){
            Array.prototype.push.apply(list, resultList);
            refreshName(list);
            doCallback({isSuccess:true, devices:list});
          });
        } else {
          refreshName(list);
          doCallback({isSuccess:true, devices:list});
        }
      },
      onerror:function(code, message){
        doCallback({isSuccess:false, error:{code:code, message:message}});
      }
    });
  }

  function getSelectedPowerMeterId(deviceId){
    return storeService.getPowermeterId(deviceId);
  }

  function setPowermeterListener(listener){
    powermeterListener = listener;
  }

  function setPowerMeter(deviceId, powermeterId){
    storeService.setPowermeterId(deviceId, powermeterId);
  }

  function doCallback(result){
    if(powermeterListener !== undefined){
      registPowermeters(result.devices);
    }
    angular.forEach(callbacks, function(callback){
      setTimeout(callback(result), 0);
    });
    callbacks = [];
    requesting = false;
  }

  function makeDeviceList(devices, list, lightList){
    angular.forEach(devices, function(device){
      var id = device.id;
      var name = device.name;
      var scopes = device.scopes;
      if(scopes.indexOf('airconditioner') !== -1){
        list.push(airconObj(id));
      }
      if(scopes.indexOf('tv') !== -1){
        list.push(tvObj(id));
      }
      if(scopes.indexOf('light') !== -1){
        //F-PLUGはリストから除外
        if(!/F-PLUG/.test(name)){
          lightList.push(device);
        }
      }
      if(scopes.indexOf('powermeter') !== -1){
        list.push(powermeterObj(id));
      }
    });
  }

  function makeLightDeviceList(lightList, callback){
    var list = [];
    deferredObjects = [];
    for (var i = 0; i < lightList.length; i++) {
      deferredObjects.push(getLightObjs(lightList[i].id));
    }
    $.when.apply(null, deferredObjects).done(function() {
      angular.forEach(arguments, function(lights){
        angular.forEach(lights, function(light){
          list.push(lightObj(light.serviceId, light.lightId));
        });
      });
      callback(list);
    });
  }

  function getLightObjs(serviceId){
    var dfd = $.Deferred();
    lightService.fetchLight(serviceId, function(result){
      if(result.isSuccess){
        angular.forEach(result.lights, function(light){
          light.serviceId = result.serviceId;
        });
        dfd.resolve(result.lights);
      } else {
        dfd.resolve();
      }
    });
    return dfd.promise();
  }

  function registPowermeters(devices){
    var powermeters = [];
    angular.forEach(devices, function(device){
      if(device.powermeter){
        powermeters.push(device.id);
      }
    });
    powermeterListener(powermeters);
  }

  function airconObj(id){
    return {
      id: id,
      name: 'device',
      scopes: ['airconditioner'],
      icon: 'img/icon_01.png',
      powermeter: false
    };
  }

  function tvObj(id){
    return {
      id: id,
      name: 'device',
      scopes: ['tv'],
      icon: 'img/icon_03.png',
      powermeter : false
    };
  }

  function lightObj(id, lightId){
    id += '?demo_lightId:' + lightId;
    return {
      id: id,
      name: 'device',
      scopes: ['light'],
      icon: 'img/icon_02.png',
      powermeter: false
    };
  }

  function powermeterObj(id){
    return {
      id: id,
      name: 'powermeter',
      scopes: ['powermeter'],
      icon: 'img/icon_02.png',
      powermeter: true
    };
  }

  function makeName(baseWord){
    var max = 0;
    angular.forEach(list, function(device){
      if(device.name.indexOf(baseWord) !== -1){
        var lastWord = device.name.slice(-2);
        var number = Number(lastWord);
        if(!isNaN(number)){
          max = Math.max(max,number);
        }
      }
    });
    baseWord += ("0" + (max + 1)).slice(-2);
    return baseWord;
  }

  function refreshName(devices){
    var unknownList = [];
    angular.forEach(devices, function(device){
      var name = storeService.getDeviceName(device.id);
      if(name !== undefined){
        device.name = name;
      } else {
        unknownList.push(device);
      }
    });
    angular.forEach(unknownList, function(device){
      var name;
      switch(device.scopes[0]){
        case 'airconditioner':
          name = makeName('エアコン');
          break;
        case 'light':
          name = makeName('照明');
          break;
        case 'tv':
          name = makeName('テレビ');
          break;
        case 'powermeter':
          name = makeName('F-PLUG');
          break;
      }
      device.name = name;
      storeService.setDeviceName(device.id, device.name);
    });
  }

  angular.module('HomeDemo').service('devices', ['manager', 'store', 'light', devices]);
})();
