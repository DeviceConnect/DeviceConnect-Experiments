/**
 service-canvas.js
 Copyright (c) 2016 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */

(function() {

  var PROFILE_NAME = 'canvas';
  var client;
  var serviceId;
  var isReady = true;

  function canvas(manager){
    client = manager.getClient();
    this.setServiceId = setServiceId;
    this.drawImage = drawImage;
    this.deleteImage = deleteImage;
  }

  function setServiceId(id){
    serviceId = id;
  }

  function resetStatus(){
    serviceId = undefined;
    isReady = true;
  }

  function drawImage(uri, callback) {
    // if(!isReady){
    //   callback({isSuccess:false, error:{code:-101, meesage:"too many requests"}});
    //   return;
    // }
    isReady = false;

    client.request({
      serviceId:serviceId,
      method:"POST",
      profile:PROFILE_NAME,
      attribute:"drawimage",
      params: {
        uri:uri,
        mode:'scales'
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

  function deleteImage(callback){
    client.request({
      serviceId:serviceId,
      method:"DELETE",
      profile:PROFILE_NAME,
      attribute:"drawimage",
      onresult: function(result){
        isReady = true;
        if(result.isSuccess){
          callback({isSuccess:true});
        } else {
          callback({isSuccess:false, error:result.error});
        }
      }});
  }

  angular.module('LinkingDemo').service('canvas', ['manager', canvas]);
})();
