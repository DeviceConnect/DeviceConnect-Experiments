/**
 service-light.js
 Copyright (c) 2015 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */

(function() {

  var PROFILE_NAME = 'light';
  var client;
  var serviceId;
  var lightId;
  var color;
  var brightness;
  var lightName;
  var isReady = true;

  function light(manager){
    client = manager.getClient();
    this.setServiceId = setServiceId;
    this.setLightId = setLightId;
    this.setColor = setColor;
    this.setBrightness = setBrightness;
    this.resetStatus = resetStatus;
    this.fetchName = fetchName;
    this.fetchLight = fetchLight;
    this.changeStatus = changeStatus;
    this.lightOn = lightOn;
    this.lightOff = lightOff;
  }

  function setServiceId(id){
    serviceId = id;
  }

  function setLightId(id){
    lightId = id;
  }

  function setColor(value){
    color = value;
  }

  function setBrightness(value){
    brightness = value;
  }

  function setLightName(value){
    lightName = value;
  }

  function resetStatus(){
    serviceId = undefined;
    lightId = undefined;
    color = undefined;
    brightness = undefined;
    lightName = undefined;
    isReady = true;
  }

  function fetchName(serviceId, lightId, callback){
    setServiceId(serviceId);
    setLightId(lightId);
    client.request({
      serviceId: serviceId,
      method: "GET",
      profile: PROFILE_NAME,
      onresult: function(result){
        if(result.isSuccess){
          var found = false;
          angular.forEach(result.response.lights, function(light){
            if(found){return;}
            if(light.lightId === lightId){
              setLightName(light.name);
              found = true;
            }
          });
          callback(found);
        } else {
          callback(false);
        }
      }});
  }

  function fetchLight(serviceId, callback){
    client.request({
      serviceId: serviceId,
      method: "GET",
      profile: PROFILE_NAME,
      onresult: function(result){
        if(result.isSuccess){
          if(result.response.lights.length > 0){
            callback({isSuccess:true, lights:result.response.lights, serviceId:serviceId});
          } else {
            callback({isSuccess:false, error:{code:-100, message:"light not founded"}});
          }
        } else {
          callback({isSuccess:false, error:result.error});
        }
      }});
  }

  function changeStatus(callback) {
    if(!isReady){
      callback({isSuccess:false, error:{code:-101, meesage:"too many requests"}});
      return;
    }
    isReady = false;
    var params = {lightId:lightId, name:lightName};
    if(color !== undefined){
      params.color = color;
    }
    if(brightness !== undefined){
      params.brightness = brightness;
    }
    client.request({
      serviceId:serviceId,
      method:"PUT",
      profile:PROFILE_NAME,
      params: params,
      onresult: function(result){
        isReady = true;
        if(result.isSuccess){
          callback({isSuccess:true});
        } else {
          callback({isSuccess:false, error:result.error});
        }
      }});
  }

  function lightOn(callback) {
    if(!isReady){
      callback({isSuccess:false, error:{code:-101, meesage:"too many requests"}});
      return;
    }
    isReady = false;
    client.request({
      serviceId:serviceId,
      method:"POST",
      profile:PROFILE_NAME,
      params: {
        lightId:lightId
      },
      onresult: function(result){
        isReady = true;
        if(result.isSuccess){
          callback({isSuccess:true});
        } else {
          callback({isSuccess:false, error:result.error});
        }
      }});
  }

  function lightOff(callback) {
    if(!isReady){
      callback({isSuccess:false, error:{code:-101, meesage:"too many requests"}});
      return;
    }
    isReady = false;
    client.request({
      serviceId:serviceId,
      method:"DELETE",
      profile:PROFILE_NAME,
      params: {
        lightId:lightId
      },
      onresult: function(result){
        isReady = true;
        if(result.isSuccess){
          callback({isSuccess:true});
        } else {
          callback({isSuccess:false, error:result.error});
        }
      }});
  }

  angular.module('HomeDemo').service('light', ['manager', light]);
})();
