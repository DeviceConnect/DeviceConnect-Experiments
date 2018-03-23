(function() {
  'use strinct';

  var constants = {
    DEBUG: false,

    applicationName: 'Demo Web',

    scopes: [
      'servicediscovery',
      'system',
      'health'
    ],

    manager: {
      android: {
        minVersion: '1.0.6',
        packageName: 'org.deviceconnect.android.manager',
        name: 'Device Web API Manager'
      },
      ios: {
        minVersion: '1.0.3',
        appId: '994422987'
      }
    },

    plugins: [
      {
        packageName: "org.deviceconnect.android.deviceplugin.hue",
        name: "hue",
        supports: ['light']
      },
      {
        packageName: "org.deviceconnect.android.deviceplugin.sphero",
        name: "Sphero",
        supports: ['light']
      },
      {
        packageName: "org.deviceconnect.android.deviceplugin.health",
        name: "Health",
        supports: ['health']
      },
      {
        packageName: "org.deviceconnect.android.deviceplugin.hvc",
        name: "HVC",
        supports: ['humandetect']
      },
      {
        packageName: "org.deviceconnect.android.deviceplugin.irkit",
        name: "IRKit",
        supports: ['remote_controller']
      },
      {
        packageName: "com.sonycsl.Kadecot",
        name: "Kadecot",
        supports: ['kadecot']
      },
    ],

    demos: {
      light: {
        profiles: ['light'],
        scopes: ['light'],
        path: '/light'
      },
      heartrate: {
        profiles: ['health'],
        scopes: ['heartRate'],
        path: '/heartrate'
      },
      thermometer: {
        profiles: ['health'],
        scopes: ['thermometer'],
        path: '/thermometer'
      },
      bloodpressure: {
        profiles: ['health'],
        scopes: ['bloodpressure'],
        path: '/bloodpressure'
      },
      weightscale: {
        profiles: ['health'],
        scopes: ['weightscale'],
        path: '/weightscale'
      },
      face: {
        profiles: ['humandetect'],
        scopes: ['humandetect'],
        path: '/face'
      },
      remote: {
        profiles: ['remote_controller'],
        scopes: ['remote_controller'],
        path: '/remote'
      }
    }
  };

  angular.module('demoweb').constant('demoConstants', constants);
})();