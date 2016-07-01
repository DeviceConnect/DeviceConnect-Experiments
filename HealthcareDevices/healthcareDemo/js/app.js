angular.module('demoweb', ['ngRoute', 'ngSanitize', 'ui.bootstrap'])
  .config(['$routeProvider', function($routeProvider) {
    $routeProvider
    .when('/', {
      templateUrl: 'app/content-list.html',
      controller: 'demoListCtrl'
    })
    .when('/error/:errorCode', {
      templateUrl: 'app/content-error.html',
      controller: 'errorCtrl'
    })
    .when('/launch', {
      templateUrl: 'app/content-launch.html',
      controller: 'launchCtrl'
    })
    .when('/prompt/settings/:demoName', {
      templateUrl: 'app/content-prompt-settings.html',
      controller: 'promptSettingsCtrl'
    })
    .when('/settings', {
      templateUrl: 'app/content-settings-all.html',
      controller: 'settingsCtrl'
    })
    .when('/settings/:demoName', {
      templateUrl: 'app/content-settings.html',
      controller: 'settingsCtrl'
    })
    .when('/settings/:demoName/:profileName', {
      templateUrl: 'app/content-settings.html',
      controller: 'settingsCtrl'
    })
    .when('/devices', {
      templateUrl: 'app/content-device-checkbox.html',
      controller: 'DeviceListController'
    })
    .when('/devices/:profileName', {
      templateUrl: 'app/content-device-checkbox.html',
      controller: 'DeviceListController'
    })
    .when('/radio', {
      templateUrl: 'app/content-device-radio.html',
      controller: 'DeviceRadioController'
    })
    .when('/radio/:demoName/:profileName', {
      templateUrl: 'app/content-device-radio.html',
      controller: 'DeviceRadioController'
    })
    .when('/radio/:demoName/:profileName/:subProfileName', {
      templateUrl: 'app/content-device-radio.html',
      controller: 'DeviceRadioController'
    })
    .when('/light', {
      templateUrl: 'app/content-light.html',
        controller: 'LightController'
    })
    .when('/light/select', {
      templateUrl: 'app/content-light-select.html',
        controller: 'SelectLightController'
    })
    .when('/heartrate', {
      templateUrl: 'app/content-heartrate.html',
      controller: 'HeartRateController'
    })
    .when('/thermometer', {
      templateUrl: 'app/content-thermometer.html',
      controller: 'ThermometerController'
    })
    .when('/bloodpressure', {
      templateUrl: 'app/content-bloodpressure.html',
      controller: 'BloodpressureController'
    })
    .when('/weightscale', {
      templateUrl: 'app/content-weightscale.html',
      controller: 'WeightscaleController'
    })
    .when('/face', {
      templateUrl: 'app/content-face.html',
      controller: 'FaceController'
    })
    .when('/remote/controller', {
      templateUrl: 'app/content-remote-controller.html',
      controller: 'RemoteController'
    })
    .when('/remote', {
      templateUrl: 'app/content-remote-select.html',
      controller: 'RemoteSelectController'
    })
    .when('/remote/command', {
      templateUrl: 'app/content-remote-command.html',
      controller: 'RemoteCommandController'
    })
    .when('/trial/manager/install', {
      templateUrl: 'trial/app/content-manager-install.html',
      controller: 'trialManagerInstallCtrl'
    })
    .when('/trial/plugin/install/:package', {
      templateUrl: 'trial/app/content-plugin-install.html',
      controller: 'trialPluginInstallCtrl'
    })
    .otherwise({redirectTo: '/'});
  }]);
