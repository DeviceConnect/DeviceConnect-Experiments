/**
 client.js
 Copyright (c) 2016 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */

/**
 * Constructor of Client.
 * Loads its settings from Cookie when an instance is constructed.
 * @public
 * @class Client
 */
var Client = function(host) {

  if (host) {
    this.setHost(host);
  }
  this.lastKnownDevices = [];
  this.releasedPlugins = [];
  this.installedPlugins = [];

  var requestInternal = function(req) {

  };
};

Client.prototype.setHost = function(host) {
  dConnect.setHost(host);
};

Client.prototype.setApplicationName = function(name) {
  this.applicationName = name;
};

Client.prototype.setScopes = function(scopes) {
  this.scopes = scopes;
};

Client.prototype.setReleasedPlugins = function(plugins) {
  this.releasedPlugins = plugins;
};

Client.prototype.setInstalledPlugins = function(plugins) {
  this.installedPlugins = plugins;
};

Client.prototype.getPlugins = function(filter) {
  var list = [],
      i,
      self = this;
  function isInstalled(packageName) {
    var i;
    for (i = 0; i < self.installedPlugins.length; i++) {
      if (packageName === self.installedPlugins[i].packageName) {
        return true;
      }
    }
    return false;
  }

  for (i = 0; i < self.installedPlugins.length; i++) {
    list.push(self.installedPlugins[i]);
  }
  for (i = 0; i < self.releasedPlugins.length; i++) {
    if (!isInstalled(self.releasedPlugins[i].packageName)) {
      list.push(self.releasedPlugins[i]);
    }
  }
  if (!filter || !filter.profiles) {
    return list;
  }

  return list.filter(function(p) {
    var profiles = filter.profiles,
        scopes = p.supports,
        i, j, found;
    for (i = 0; i < profiles.length; i++) {
      found = false;
      loop:
      for (j = 0; j < scopes.length; j++) {
        if (profiles[i] === scopes[j]) {
          found = true;
          break loop;
        }
      }
      if (!found) {
        return false;
      }
      if (filter.installed === true && p.installed !== true) {
        return false;
      }
    }
    return true;
  });
};

Client.prototype.authorize = function(callback) {
  var self = this;
  dConnect.authorization(self.scopes, self.applicationName, function(clientId, accessToken) {
    self.settings.accessToken = accessToken;
    callback.onsuccess();
  }, function(errorCode, errorMessage) {
    callback.onerror(errorCode, errorMessage);
  });
};

Client.prototype.checkAvailability = function(callback) {
  dConnect.checkDeviceConnect(function(version) {
    callback.onsuccess(version);
  }, function(errorCode, errorMessage) {
    callback.onerror(errorCode, errorMessage);
  });
};

Client.prototype.startManager = function() {
  dConnect.setLaunchListener(function(version){});
  dConnect.startManager();
};

Client.prototype.openSettingWindow = function(opt) {
  var builder = new dConnect.URIBuilder();
  builder.setProfile('system');
  builder.setInterface('device');
  builder.setAttribute('wakeup');
  builder.addParameter('pluginId', opt.pluginId);

  dConnect.put(builder.build(), null, null, opt.onsuccess, opt.onerror);
};

Client.prototype.request = function(req) {
  req.onresult = req.onresult || function() {};
  console.log('request');
  console.log(req);
  var builder = new dConnect.URIBuilder();
  builder.setProfile(req.profile);
  if (req.interface) {
    builder.setInterface(req.interface);
  }
  if (req.attribute) {
    builder.setAttribute(req.attribute);
  }
  builder.setServiceId(req.serviceId);
  if (this.settings.accessToken) {
    builder.setAccessToken(this.settings.accessToken);
  }
  for (var key in req.params) {
    builder.addParameter(key, req.params[key]);
  }
  var uri = builder.build();
  console.log('uri:' + uri);
  var self = this;
  dConnect.sendRequest(req.method, uri, null, null,
    function(json) {
      req.onresult({
        serviceId: req.serviceId,
        isSuccess: true,
        response: json
      });
    },
    function(errorCode, errorMessage) {
      switch (errorCode) {
      case dConnect.constants.ErrorCode.NOT_FOUND_CLIENT_ID:
      case dConnect.constants.ErrorCode.EMPTY_ACCESS_TOKEN:
      case dConnect.constants.ErrorCode.SCOPE:
        self.authorize({
          onsuccess: function() {
            self.request(req);
          },
          onerror: function(errorCode, errorMessage) {
            notifyError(errorCode, errorMessage);
          }
        });
        break;
      default:
        notifyError(errorCode, errorMessage);
        break;
      }
    });

  function notifyError(code, message) {
    req.onresult({
      serviceId: req.serviceId,
      isSuccess: false,
      error: { code: code, message: message }
    });
  }
};

Client.prototype.sendRequestList = function(option) {
  var i;
  for (i = 0; i < option.requests.length; i++) {
    this.request(option.requests[i]);
  }
};

Client.prototype.broadcast = function(req) {
  var i,
      count = 0,
      successes = [],
      errors = [],
      onresult = function(result) {
        if (result.isSuccess) {
          successes.push(result);
        } else {
          errors.push(result);
        }
        if (++count == req.serviceIds.length) {
          req.onresult({
            isSuccess: errors.length === 0,
            successes: successes,
            errors: errors
          });
        }
      };
  for (i = 0; i < req.serviceIds.length; i++) {
    var serviceId = req.serviceIds[i];
    this.request({
      method: req.method,
      profile: req.profile,
      interface: req.interface,
      attribute: req.attribute,
      params: req.params,

      serviceId: serviceId,
      onresult: onresult
    });
  }
};

Client.prototype.discoverPlugins = function(callback) {
  var self = this,
      builder = new dConnect.URIBuilder();

  builder.setProfile('system');
  if (self.settings.accessToken) {
    builder.setAccessToken(self.settings.accessToken);
  }
  dConnect.get(builder.build(), null, function(json) {
    var plugins = json.plugins;
    for (var i = 0; i < plugins.length; i++) {
      plugins[i].installed = true;
    }
    self.setInstalledPlugins(plugins);

    callback.onsuccess(self.getPlugins());
  }, function(errorCode, errorMessage) {
    switch (errorCode) {
    case dConnect.constants.ErrorCode.NOT_FOUND_CLIENT_ID:
    case dConnect.constants.ErrorCode.EMPTY_ACCESS_TOKEN:
    case dConnect.constants.ErrorCode.SCOPE:
      self.authorize({
        onsuccess: function() {
          self.discoverPlugins(callback);
        },
        onerror: function(errorCode, errorMessage) {
          callback.onerror(errorCode, errorMessage);
        }
      });
      break;
    default:
      callback.onerror(errorCode, errorMessage);
      break;
    }
  });
};

Client.prototype.getLastKnownDevices = function() {
  return this.lastKnownDevices;
};

Client.prototype.containLastKnownDevices = function(service) {
  for (var i = 0; i < this.lastKnownDevices.length; i++) {
    var id = this.lastKnownDevices[i].id;
    if (id === service.id) {
      return true;
    }
  }
  return false;
};

Client.prototype.removeLastKnownDevices = function(service) {
  for (var i = 0; i < this.lastKnownDevices.length; i++) {
    var id = this.lastKnownDevices[i].id;
    if (id === service.id) {
      this.lastKnownDevices.splice(i, 1);
      return;
    }
  }
};

Client.prototype.cleanupLastKnownDevices = function(services) {
  var removeServices = [];
  for (var i = 0; i < this.lastKnownDevices.length; i++) {
    var id = this.lastKnownDevices[i].id;
    var found = false;
    for (var j = 0; j < services.length; j++) {
      if (id === services[j].id) {
        found = true;
      }
    }
    if (!found) {
      removeServices.push(this.lastKnownDevices[i]);
    }
  }
  if (removeServices.length > 0) {
    for (i = 0; i < removeServices.length; i++) {
      this.removeLastKnownDevices(removeServices[i]);
    }
  }
};

Client.prototype.discoverDevices = function(callback) {
  var self = this;

  dConnect.discoverDevices(self.settings.accessToken, function(json) {
    self.cleanupLastKnownDevices(json.services);
    console.log("from SDK");
    console.log(json);
    var count = 0;
    var length = json.services.length;
    if (length === 0) {
      callback.onsuccess(self.lastKnownDevices);
    } else {
      var serviceInfomartionCallback = function(service) {
        if (service) {
          self.lastKnownDevices.push(service);
        }
        count++;
        if (count == length) {
          callback.onsuccess(self.lastKnownDevices);
        }
      };
      for (var i = 0; i < json.services.length; i++) {
        if (self.containLastKnownDevices(json.services[i])) {
          count++;
          if (count == length) {
            callback.onsuccess(self.lastKnownDevices);
          }
        } else {
          self.serviceInfomartion(json.services[i], serviceInfomartionCallback);
        }
      }
    }
  }, function(errorCode, errorMessage) {
    switch (errorCode) {
    case dConnect.constants.ErrorCode.NOT_FOUND_CLIENT_ID:
    case dConnect.constants.ErrorCode.EMPTY_ACCESS_TOKEN:
    case dConnect.constants.ErrorCode.SCOPE:
      self.authorize({
        onsuccess: function() {
          self.discoverDevices(callback);
        },
        onerror: callback.onerror
      });
      break;
    default:
      callback.onerror(errorCode, errorMessage);
      break;
    }
  });
};

Client.prototype.getDeviceById = function(id, callback) {
  this.discoverDevices({
    onsuccess: function(devices) {
      var i,
          device;
      for (i = 0; i < devices.length; i++) {
        if (id === devices[i].id) {
          device = devices[i];
          break;
        }
      }
      callback.onsuccess(device);
    },
    onerror: function(errorCode, error) {
      callback.onerror(errorCode, error);
    }
  });
};

Client.prototype.serviceInfomartion = function(service, callback) {
  var self = this;

  dConnect.getSystemDeviceInfo(service.id, self.settings.accessToken, function(status, header, responseText) {
    var json = JSON.parse(responseText);
    console.log(json);
    console.log(service);
    if (json.result === 0) {
      // service.scopes = json.supports;
      callback(service);
    } else {
      callback(undefined);
    }
  }, function(errorCode, errorMessage) {
    callback(undefined);
  });
};

Client.prototype.connectWebSocket = function(callback) {
  dConnect.connectWebSocket(this.settings.sessionKey, callback);
};

Client.prototype.isConnectedWebSocket = function() {
  return dConnect.isConnectedWebSocket();
};

Client.prototype.disconnectWebSocket = function() {
  dConnect.disconnectWebSocket();
};
