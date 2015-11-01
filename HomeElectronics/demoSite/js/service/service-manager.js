/**
 service-manager.js
 Copyright (c) 2015 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */

(function() {

  function manager(store, demoConstants){
    var client = new Client(getIpString());
    client.settings = {
      sessionKey: store.getSessionKey(),
      accessToken: store.getToken()
    };
    client.setApplicationName(demoConstants.applicationName);
    client.setScopes(demoConstants.scopes);

    this.authorize = function(){
      client.authorize({
        onsuccess:function(){
          store.setSessionKey(client.settings.sessionKey);
          store.setToken(client.settings.accessToken);
        },
        onerror:function(errorCode, errorMessage){
          console.log('[Error] code:' + errorCode + ' message:' + errorMessage);
        }
      });
    };
    this.discovery = function(callback){
      client.discoverDevices(callback);
    };
    this.getClient = function(){
      return client;
    };
  }

  function getIpString() {
    if (1 < document.location.search.length) {
      var query = document.location.search.substring(1);
      var parameters = query.split('&');
      for (var i = 0; i < parameters.length; i++) {
        var element = parameters[i].split('=');
        var paramName = decodeURIComponent(element[0]);
        var paramValue = decodeURIComponent(element[1]);
        if (paramName == 'ip') {
          return paramValue;
        }
      }
    }
    return location.hostname;
  }

  angular.module('HomeDemo').service('manager', ['store', 'demoConstants', manager]);
})();
