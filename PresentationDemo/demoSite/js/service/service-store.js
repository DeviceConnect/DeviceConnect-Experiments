/**
 service-store.js
 Copyright (c) 2016 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */

(function() {

  var KEY_SETTINGS = 'linkingdemo';

  function store(){
    this.setTargetLink = function(targetLink){transact(function(cookie){cookie.targetLink = targetLink;});};
    this.getTargetLink = function(){return load().targetLink;};
    this.setHostName = function(hostName){transact(function(cookie){cookie.hostName = hostName;});};
    this.getHostName = function(){return load().hostName;};
    this.setSessionKey = function(sessionKey){transact(function(cookie){cookie.settionKey = sessionKey;});};
    this.getSessionKey = function(){return load().sessionKey;};
    this.setToken = function(accessToken){transact(function(cookie){cookie.accessToken = accessToken;});};
    this.getToken = function(){return load().accessToken;};
    this.setUserName = function(userName){transact(function(cookie){cookie.userName = userName;});};
    this.getUserName = function(){return load().userName;};
    this.setSlideName = function(slideName){transact(function(cookie){cookie.slideName = slideName;});};
    this.getSlideName = function(){return load().slideName;};
    this.setSuffix = function(suffix){transact(function(cookie){cookie.suffix = suffix;});};
    this.getSuffix = function(){return load().suffix;};

    this.setTimers = function(timers){transact(function(cookie){cookie.timers = timers;});};
    this.getTimers = function(){return load().timers;};
    this.setPlayers = function(players){ transact(function(cookie){cookie.players = players;});};
    this.getPlayers = function(){return load().players;};

    this.setDevices = function(devices){transact(function(cookie){cookie.devices = devices;});};
    this.getDevices = function(){return load().devices;};

    this.clearAll = clearAll;
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
        cookie = {hostName:'localhost',userName:'KeiichiroFujii',slideName:'vdc-5th-ntt'};
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

  angular.module('LinkingDemo').service('store', [store]);
})();
