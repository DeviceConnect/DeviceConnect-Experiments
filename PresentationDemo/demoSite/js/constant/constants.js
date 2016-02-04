/**
 constants.js
 Copyright (c) 2016 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */

(function() {
  'use strinct';

  var constants = {
    applicationName: 'Linking Demo',
    scopes: [
      'servicediscovery',
      'serviceinformation',
      'system',
      'light'
    ],
    manager: {
      android: {
        minVersion: '1.0.6',
        packageName: 'org.deviceconnect.android.manager',
        name: 'Device Web API Manager'
      },
      ios: {
        minVersion: '1.0.3',
        appId: '994422987',
        name: 'Device Web API Browswer'
      }
    },
  };

  angular.module('LinkingDemo').constant('demoConstants', constants);
})();
