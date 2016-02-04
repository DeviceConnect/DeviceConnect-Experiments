/**
 service-utils.js
 Copyright (c) 2016 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */

(function() {

  var constants;

  function utilsService(demoConstants){
    this.isMobile = isMobile;
    this.isAndroid = isAndroid;
    this.isiOS = isIOS;
    this.createUriForIOS = createUriForIOS;
    this.constants = demoConstants;
  }

  function isMobile() {
    return isAndroid() || isIOS();
  }

  function isAndroid() {
    var ua = navigator.userAgent;
    if (/Android/.test(ua)) {
      return true;
    }
    return false;
  }

  function isIOS() {
    var ua = navigator.userAgent;
    if(/iPhone/.test(ua)) {
      return true;
    } else if(/iPad/.test(ua)) {
      return true;
    }
    return false;
  }

  function createUriForIOS() {
    console.log(constants);
    return 'itms-apps://itunes.apple.com/app/id' + this.constants.manager.ios.appId + '?ls=1&mt=8';
  }

  angular.module('LinkingDemo').service('utils', ['demoConstants', utilsService]);
})();
