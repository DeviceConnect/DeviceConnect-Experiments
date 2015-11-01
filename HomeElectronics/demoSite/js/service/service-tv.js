/**
 service-tv.js
 Copyright (c) 2015 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */

(function() {

  var PROFILE_NAME = 'tv';
  var client;
  var serviceId;

  function tv(manager){
    client = manager.getClient();
    this.setServiceId = setServiceId;
    this.tvOn = tvOn;
    this.channelNext = channelNext;
    this.channelPrevious = channelPrevious;
    this.channelTuning = channelTuning;
    this.volumeUp = volumeUp;
    this.volumeDown = volumeDown;
    this.broadcastwaveDigital = broadcastwaveDigital;
    this.broadcastwaveBS = broadcastwaveBS;
    this.broadcastwaveCS = broadcastwaveCS;
  }

  function setServiceId(id){
    serviceId = id;
  }

  function requestAPI(attribute, params, callback){
    var reqObj = {
      serviceId:serviceId,
      method:"PUT",
      profile:PROFILE_NAME,
      onresult: function(result){
        if(result.isSuccess){
          callback({isSuccess:true});
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

  function tvOn(callback){ requestAPI(undefined, undefined, callback); }
  function channelNext(callback){ requestAPI('channel', {control:'next'}, callback); }
  function channelPrevious(callback){ requestAPI('channel', {control:'previous'}, callback); }
  function channelTuning(tuning, callback){ requestAPI('channel', {tuning:tuning}, callback); }
  function volumeUp(callback){ requestAPI('volume', {control:'up'}, callback); }
  function volumeDown(callback){ requestAPI('volume', {control:'down'}, callback); }
  function broadcastwaveDigital(callback){ requestAPI('broadcastwave', {select:'DTV'}, callback); }
  function broadcastwaveBS(callback){ requestAPI('broadcastwave', {select:'BS'}, callback); }
  function broadcastwaveCS(callback){requestAPI('broadcastwave', {select:'CS'}, callback); }

  angular.module('HomeDemo').service('tv', ['manager', tv]);
})();
