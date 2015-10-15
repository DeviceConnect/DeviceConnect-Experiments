/**
 service-aircon.js
 Copyright (c) 2015 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */

(function() {

  var PROFILE_NAME = 'airconditioner';
  var client;
  var serviceId;

  function aircon(manager){
    client = manager.getClient();
    this.setServiceId = setServiceId;
    this.getRoomTemperature = getRoomTemperature;
    this.getTemperature = getTemperature;
    this.getPowerStatus = getPowerStatus;
    this.getModeStatus = getModeStatus;
    this.airconOn = airconOn;
    this.airconOff = airconOff;
    this.modeAuto = modeAuto;
    this.modeDry = modeDry;
    this.modeCool = modeCool;
    this.modeHot = modeHot;
    this.modeAir = modeAir;
    this.setTemperature = setTemperature;
  }

  function setServiceId(id){
    serviceId = id;
  }

  function requestAPI(method ,attribute, params, callback){
    var reqObj = {
      serviceId:serviceId,
      method:method,
      profile:PROFILE_NAME,
      onresult: function(result){
        if(result.isSuccess){
          callback({isSuccess:true, response:result.response});
        } else {
          callback({isSuccess:false, error:result.error});
        }
      }
    };
    if(attribute !== undefined){
      reqObj.attribute = attribute;
    }
    if(params !== undefined){
      reqObj.params = params;
    }
    client.request(reqObj);
  }

  function getRoomTemperature(callback){ requestAPI('GET', 'roomtemperature', undefined, callback); }
  function getTemperature(callback){ requestAPI('GET', 'temperaturevalue', undefined, callback); }
  function getPowerStatus(callback){ requestAPI('GET', undefined, undefined, callback); }
  function getModeStatus(callback){ requestAPI('GET', 'operationmodesetting', undefined, callback); }
  function airconOn(callback){ requestAPI('PUT', undefined, undefined, callback); }
  function airconOff(callback){ requestAPI('DELETE', undefined, undefined, callback); }
  function modeAuto(callback){ requestAPI('PUT', 'operationmodesetting', {operationmodesetting:'Automatic'}, callback); }
  function modeDry(callback){ requestAPI('PUT', 'operationmodesetting', {operationmodesetting:'Dehumidification'}, callback); }
  function modeCool(callback){ requestAPI('PUT', 'operationmodesetting', {operationmodesetting:'Cooling'}, callback); }
  function modeHot(callback){ requestAPI('PUT', 'operationmodesetting', {operationmodesetting:'Heating'}, callback); }
  function modeAir(callback){ requestAPI('PUT', 'operationmodesetting', {operationmodesetting:'AirCirculator'}, callback); }
  function setTemperature(temperature, callback){ requestAPI('PUT', 'temperaturevalue', {temperaturevalue:temperature}, callback); }

  angular.module('HomeDemo').service('aircon', ['manager', aircon]);
})();
