/**
 service-vibration.js
 Copyright (c) 2016 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */

(function() {

  var PROFILE_NAME = 'vibration';
  var client;
  var serviceId;
  var isReady = true;

  function vibration(manager){
    client = manager.getClient();
    this.setServiceId = setServiceId;
    this.start = start;
  }

  function setServiceId(id){
    serviceId = id;
  }

  function resetStatus(){
    serviceId = undefined;
    isReady = true;
  }

  function start(pattern, callback) {
    isReady = false;

    client.request({
      serviceId:serviceId,
      method:"PUT",
      profile:PROFILE_NAME,
      attribute:"vibrate",
      params:{pattern:pattern},
      onresult: function(result){
        isReady = true;
        if(result.isSuccess){
          callback({isSuccess:true});
        } else {
          callback({isSuccess:false, error:result.error});
        }
      }});
  }

  angular.module('LinkingDemo').service('vibration', ['manager', vibration]);
})();
