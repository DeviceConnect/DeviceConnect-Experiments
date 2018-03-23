(function() {
  angular.module('demoweb')
    .service('lightService', ['$rootScope', function($rootScope) {
      var service = {
        lights: [],
        addLight: function (light) {
          service.lights.push(light);
        },
        removeAll: function() {
          service.lights = [];
        },
        setLight: function(list) {
          service.lights = [];
          service.lights.concat(list);
        },
        discover: function(client, callback) {
          var devices = client.getLastKnownDevices();
          if (devices && devices.length > 0) {
            this.discoverLights(client, devices, callback);
          } else {
            this.discoverDevices(client, callback);
          }
        },
        discoverDevices: function(client, callback) {
          var self = this;
          client.discoverDevices({
            onsuccess: function(services) {
              self.discoverLights(client, services, callback);
            },
            onerror: function(errorCode, errorMessage) {
              callback.onerror(errorCode, errorMessage);
            }
          })
        },
        discoverLights: function(client, devices, callback) {
          var self = this;
          var serviceIds = [];
          for (var i = 0; i < devices.length; i++) {
            if (devices[i].scopes.lastIndexOf('light') >= 0) {
              serviceIds.push(devices[i].id);
            }
          }
          var lightMap = {};
          client.request({
            "method": "GET",
            "profile": "light",
            "devices": serviceIds,
            "onsuccess": function(id, json) {
              if (json.lights) {
                lightMap[id] = [];
                for (var i = 0; i < json.lights.length; i++) {
                  lightMap[id].push(json.lights[i]);
                }
              }
            },
            "onerror": function(id, errorCode, errorMessage) {
              callback.onerror(errorCode, errorMessage);
            },
            "oncomplete": function() {
              self.removeAll();
              for (var serviceId in lightMap) {
                var lights = lightMap[serviceId];
                for (var i in lights) {
                  var light = lights[i];
                  self.addLight({
                    'name': light.name,
                    'serviceId': serviceId,
                    'light': light,
                    'checked': true
                  });
                }
              }
              callback.oncomplete(self.lights);
            }
          });
        }
      }
      return service;
    }]);
})();