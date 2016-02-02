/**
 service-discovery.js
 Copyright (c) 2016 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */

(function() {

  var mManager;
  var mStore;
  var mLight;

  function discoveryService(manager, store, light){
    mManager = manager;
    mStore = store;
    mLight = light;
    this.getPlayers = getPlayers;
    this.getTargets = getTargets;
    this.getDevices = getDevices;
  }

  function getPlayers(cached, callback){
    getDevices(['canvas'],cached,function(result){
      if(!result.isSuccess){
        callback(result);
        return;
      }
      var players = [];
      angular.forEach(result.devices, function(device){
        players.push({
          serviceId:device.id,
          name:device.name
        });
      });
      callback({isSuccess:true, devices:players});
    });
  }

  function getTargets(typeName, cached, callback){
    getDevices(makeScopes(typeName),cached,function(result){
      if(!result.isSuccess){
        callback(result);
        return;
      }
      var targets = [];
      angular.forEach(result.devices, function(device){
        targets.push({
          serviceId:device.id,
          name:device.name,
          lightId:device.lightId
        });
      });
      callback({isSuccess:true, devices:targets});
    });
  }

  function getDevices(profiles, cached, callback){
    var devices = cached ? mStore.getDevices() : undefined;
    if(devices && devices.length > 0){
      callback({isSuccess:true,devices:extractDevicesForProfiles(profiles, devices)});
      return;
    }
    mManager.setHostName(mStore.getHostName());
    mManager.discovery({
      onsuccess: function(devices){
        var deferredObjects = [];
        var results = [];
        angular.forEach(devices, function(device){
          console.log(device);
          if(hasProfile('light',device)){
            deferredObjects.push(fetchLightId(device, results));
          } else {
            results.push(device);
          }
        });
        $.when.apply(null, deferredObjects).done(function(){
            mStore.setDevices(results);
            callback({isSuccess:true,devices:extractDevicesForProfiles(profiles, results)});
        });
      },onerror: function(errorCode, errorMessage){
        callback({isSuccess:false,errorCode:errorCode,errorMessage:errorMessage});
      }
    });
  }

  function fetchLightId(device, results){
    var dfd = $.Deferred();
    mLight.fetchLight(device.id, function(result){
      if(!result.isSuccess){
        console.log('fetch failed');
        dfd.resolve();
        return;
      }
      console.log(result);
      angular.forEach(result.lights, function(light){
        results.push({id:device.id,name:(light.name+':'+light.lightId),lightId:light.lightId,scopes:device.scopes});
      });
      dfd.resolve();
    });
    return dfd.promise();
  }

  function extractDevicesForProfiles(profiles, devices){
    var list = [];
    angular.forEach(devices, function(device){
      var matched = false;
      angular.forEach(profiles, function(profile){
        if(matched){return;}
        if(hasProfile(profile, device)){
          matched = true;
        }
      });
      if(matched){
        list.push(device);
      }
    });
    return list;
  }

  function hasProfile(profile, device){
    return ($.inArray(profile ,device.scopes) !== -1);
  }

  function makeScopes(typeName){
    var scopes = [];
    if(typeName.match(/色/)){
      scopes.push('light');
    }
    if(typeName.match(/消灯/)){
      scopes.push('light');
    }
    if(typeName.match(/バイブレーション/)){
      scopes.push('vibration');
    }
    return scopes;
  }

  angular.module('LinkingDemo').service('discovery', ['manager','store','light',discoveryService]);
})();
