/**
 service-store.js
 Copyright (c) 2015 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */

(function() {

  var KEY_SETTINGS = 'homedemo';

  function store(){
    this.setDeviceName = function(identity, deviceName){
      transact(function(cookie){
        var devices = cookie.devices;
        var found = false;
        angular.forEach(devices, function(device){
          if(found){return;}
          if(device.identity === identity){
            found = true;
            device.name = deviceName;
          }
        });
        if(!found){
          devices.push({identity:identity,name:deviceName,powermeterId:undefined});
        }
      });
    };
    this.getDeviceName = function(identity){
      var devices = load().devices;
      var found = false;
      var deviceName;
      angular.forEach(devices, function(device){
        if(found){return;}
        if(device.identity === identity){
          found = true;
          deviceName = device.name;
        }
      });
      return deviceName;
    };
    this.setPowermeterId = function(identity, powermeterId){
      transact(function(cookie){
        var devices = cookie.devices;
        var found = false;
        angular.forEach(devices, function(device){
          if(found){return;}
          if(device.identity === identity){
            found = true;
            device.powermeterId = powermeterId;
          }
        });
        if(!found){
          devices.push({identity:identity,name:undefined,powermeterId:powermeterId});
        }
      });
    };
    this.getPowermeterId = function(identity){
      var devices = load().devices;
      var found = false;
      var powermeterId;
      angular.forEach(devices, function(device){
        if(found){return;}
        if(device.identity === identity){
          found = true;
          powermeterId = device.powermeterId;
        }
      });
      return powermeterId;
    };
    this.setSessionKey = function(sessionKey){
      transact(function(cookie){
        cookie.settings.settionKey = sessionKey;
      });
    };
    this.getSessionKey = function(){
      return load().settings.sessionKey;
    };
    this.setToken = function(accessToken){
      transact(function(cookie){
        cookie.settings.accessToken = accessToken;
      });
    };
    this.getToken = function(){
      return load().settings.accessToken;
    };
  }

  function transact(perform){
    try{
      var cookie = load();
      perform(cookie);
      Cookies.set(KEY_SETTINGS, cookie);
    }catch(e){
      console.log(e);
      clearAll();
    }
  }

  function load(){
    try{
      var cookie = Cookies.getJSON(KEY_SETTINGS);
      if(cookie === undefined){
        cookie = {devices:[],settings:{sessionKey:Date.now().toString(),accessToken:undefined}};
      }
      return cookie;
    }catch(e){
      console.log(e);
      clearAll();
    }
  }

  function clearAll(){
    Cookies.remove(KEY_SETTINGS);
  }

  angular.module('HomeDemo').service('store', [store]);
})();
