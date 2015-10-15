/**
 module.js
 Copyright (c) 2015 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */

angular.module('HomeDemo', ['ngRoute', 'ngSanitize', 'angularSpinner'])
  .config(['$routeProvider', function($routeProvider) {
    $routeProvider
    .when('/', {
      templateUrl: 'html/top.html',
      controller: 'DeviceListController'
    })
    .when('/setting', {
      templateUrl: 'html/setting.html',
      controller: 'SettingListController'
    })
    .when('/graph', {
      templateUrl: 'html/graph.html',
      controller: 'TotalGraphController'
    })
    .when('/graph/:id', {
      templateUrl: 'html/graph.html',
      controller: 'DeviceGraphController'
    })
    .when('/setting/device/:id',{
      templateUrl: 'html/device-setting.html',
      controller: 'SettingController'
    })
    .when('/setting/powermeter/:id',{
      templateUrl: 'html/powermeter.html',
      controller: 'PowerMeterController'
    })
    .when('/aircon/:id', {
      templateUrl: 'html/aircon.html',
      controller: 'AirConditionerController'
    })
    .when('/tv/:id', {
      templateUrl: 'html/tv.html',
      controller: 'TVController'
    })
    .when('/light/:id', {
      templateUrl: 'html/light.html',
      controller: 'LightController'
    })
    .otherwise({redirectTo: '/'});
  }]);
