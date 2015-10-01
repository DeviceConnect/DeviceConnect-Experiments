/**
 constants.js
 Copyright (c) 2015 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */

(function() {
  'use strinct';

  var constants = {
    applicationName: 'Home Demo',
    scopes: [
      'servicediscovery',
      'serviceinformation',
      'system',
      'light',
      'powermeter',
      'tv',
      'air_conditioner'
    ]
  };

  angular.module('HomeDemo').constant('demoConstants', constants);
})();
