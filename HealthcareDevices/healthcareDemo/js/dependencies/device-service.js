(function() {

  var buffer = {};

  angular.module('demoweb')
    .service('deviceService', ['$rootScope', function($rootScope) {
      var service = {

        list: function(id) {
          var list = buffer[id];
          if (list === undefined) {
            list = {
              devices: [],
              addDevice: function (device) {
                list.devices.push(device);
              },
              removeAll: function() {
                list.devices = [];
              }
            };
            buffer[id] = list;
          }
          return list;
        },

        discoverDevices: function(client, callback) {
          client.discoverDevices({
            onsuccess: function(services) {
              callback(services);
            },
            onerror: function(errorCode, errorMessage) {
              callback([]);
            }
          });
        },

        searchDevices: function(client, profileName, callback) {
          this.discoverDevices(client, function(devices) {
            var list = [];
            for (var i = 0; i < devices.length; i++) {
              if (profileName === undefined || 
                  devices[i].scopes.lastIndexOf(profileName) >= 0) {
                list.push(devices[i]);
              }
            }
            callback(list);
          });
        }
      }
      return service;
    }]);
})();