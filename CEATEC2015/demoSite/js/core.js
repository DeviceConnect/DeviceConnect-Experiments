var demoWeb = (function (parent) {
  'use strict';

  $.cookie.json = true;

  /**
   * Key of settings of Client.
   * @private
   * @const
   * @type {!string}
   */
  var KEY_SETTINGS = 'demoWeb.settings';
  
  /**
   * Loads settings of Client from Cookie.
   * @private
   * @param {!demoWeb.Client} client
   */
  var _loadSettings = function(client) {
    client.settings = $.cookie(KEY_SETTINGS) || {
      sessionKey: Date.now().toString(),
      accessToken: undefined
    };
  };

  /**
   * Stores settings of Client to Cookie.
   * @private
   * @param {!demoWeb.Client} client
   */
  var _storeSettings = function(client) {
    $.cookie(KEY_SETTINGS, client.settings);
  };

  /**
   * Constructor of Client.
   * Loads its settings from Cookie when an instance is constructed.
   * @public
   * @class Client
   * @memberof demoWeb
   */
  var Client = function(host, origin) {
    if (host) {
      this.setHost(host);
    }
    if (origin) {
      this.setExtendedOrigin(origin);
    }
  _loadSettings(this);
    this.lastKnownDevices = [];
    this.releasedPlugins = [];
    this.installedPlugins = [];
//    this.connectWebSocket(function() {});
  };
  parent.Client = Client;

  /**
   * Set a host of GotAPI Server.
   * @public
   * @memberof demoWeb.Client
   * @param {!string} host a host of Device Connect server
   */
  Client.prototype.setHost = function(host) {
    dConnect.setHost(host);
  };

  Client.prototype.getHost = function() {
    return dConnect.getHost();
  };

  Client.prototype.setExtendedOrigin = function(origin) {
    dConnect.setExtendedOrigin(origin);
  }

  /**
   * Sets an application's name.
   * The value is used as a request parameter of Grant API.
   * @public
   * @memberof demoWeb.Client
   * @param {!string} name an application's name
   */
  Client.prototype.setApplicationName = function(name) {
    this.applicationName = name;
  };

  /**
   * Sets an array of scopes.
   * The value is used as a request parameter of Grant API.
   * @public
   * @memberof demoWeb.Client
   * @param {!string[]} scopes an array of scopes
   */
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

  /**
   * Application Authorization Callback.
   * @public
   * @memberof demoWeb.Client
   * @typedef {object} AuthCallback
   * @callback
   * @prop {function} onsuccess 
   * @prop {function} onerror 
   */

  /**
   * Authorizes this application.
   * @public
   * @memberof demoWeb.Client
   * @param {demoWeb.Client.AuthCallback} callback an instance of a callback.
   */
  Client.prototype.authorize = function(callback) {
      var self = this;
      dConnect.authorization(self.scopes, self.applicationName, function(clientId, accessToken) {
        self.settings.accessToken = accessToken;
        _storeSettings(self)
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
  }

  Client.prototype.startManager = function() {
    dConnect.startManager();
  };

  Client.prototype.isAuthorization = function() {
    console.log(this.settings);
    return this.settings.accessToken != undefined;
  }

  Client.prototype.openSettingWindow = function(opt) {
    var builder = new dConnect.URIBuilder();
    builder.setProfile('system');
    builder.setInterface('device');
    builder.setAttribute('wakeup');
    builder.addParameter('pluginId', opt.pluginId);

    dConnect.put(builder.build(), null, null, opt.onsuccess, opt.onerror);
  }

  /**
   * Request to devices managed by GotAPI server.
   * @public
   * @memberof demoWeb.Client
   * @typedef {object} Request
   * @prop {!string} method
   * @prop {!string} profile
   * @prop {?string} interface
   * @prop {?string} attribute
   * @prop {?string} accessToken
   * @prop {!object[]} devices 
   * @prop {!object[]} params
   * @prop {function} onsuccess
   * @prop {function} onerror
   * @prop {function} oncomplete
   */

  /**
   * Sends requests to devices managed by GotAPI server.
   * @public
   * @memberof demoWeb.Client
   * @param {demoWeb.Client.Request} req a request
   */
  Client.prototype.request = function(req) {
    req.devices = req.devices || [];
    req.oncomplete = req.oncomplete || function() {};
    req.onsuccess = req.onsuccess || function() {};
    req.onerror = req.onerror || function() {};

    var builder = new dConnect.URIBuilder();
    if (req.profile) {
      builder.setProfile(req.profile);
    }
    if (req.interface) {
      builder.setInterface(req.interface);
    }
    if (req.attribute) {
      builder.setAttribute(req.attribute);
    }
    if (this.settings.accessToken) {
      builder.setAccessToken(this.settings.accessToken);
    }
    for (var key in req.params) {
      builder.addParameter(key, req.params[key]);
    }

    var self = this;
    var callbacks = [];
    var count = req.devices.length;
    var checkFinished = function() {
      if ((--count) === 0) {
        req.oncomplete();
      }
    };
    if (count == 0) {
      req.oncomplete();
    } else {
      for (var i = 0; i < req.devices.length; i++) {
        callbacks.push({
          id: req.devices[i],
          onsuccess: function(json) {
            req.onsuccess(this.id, json);
            checkFinished();
          },
          onerror: function(errorCode, errorMessage) {
            req.onerror(this.id, errorCode, errorMessage);
            checkFinished();
          }
        });
      }
      self.requestInternal(req.method, builder, req.devices, callbacks, 0);
    }
  };

  Client.prototype.requestInternal = function(method, builder, serviceIds, callbacks, index) {
    builder.setServiceId(serviceIds[index]);
    builder.setAccessToken(this.settings.accessToken);

    var self = this;
    var uri = builder.build();
    dConnect.sendRequest(method, uri, null, null, function(json) {
        callbacks[index].onsuccess(json);
        index++;
        if (index < serviceIds.length) {
          self.requestInternal(method, builder, serviceIds, callbacks, index);
        }
      }, function(errorCode, errorMessage) {
        switch (errorCode) {
        case dConnect.constants.ErrorCode.NOT_FOUND_CLIENT_ID:
        case dConnect.constants.ErrorCode.EMPTY_ACCESS_TOKEN:
        case dConnect.constants.ErrorCode.SCOPE:
          self.authorize({
            onsuccess: function() {
              self.requestInternal(method, builder, serviceIds, callbacks, index);
            },
            onerror: function(errorCode, errorMessage) {
              callbacks[index].onerror(errorCode, errorMessage);
              index++;
              if (index < serviceIds.length) {
                self.requestInternal(method, builder, serviceIds, callbacks, index);
              }
            }
          });
          break;
        default:
          callbacks[index].onerror(errorCode, errorMessage);
          index++;
          if (index < serviceIds.length) {
            self.requestInternal(method, builder, serviceIds, callbacks, index);
          }
          break;
        }
    });
  }

  /**
   * Callback of plugin discovery.
   * @memberof demoWeb.Client
   * @typedef {object} PluginDiscoveryCallback
   * @prop {demoWeb.Client.OnSuccessPluginDiscovery} onsuccess
   * @prop {demoWeb.Client.OnError} onerror
   */

  /**
   * Callback of device discovery.
   * @memberof demoWeb.Client
   * @typedef {object} DeviceDiscoveryCallback
   * @prop {demoWeb.Client.OnSuccessDeviceDiscovery} onsuccess
   * @prop {demoWeb.Client.OnError} onerror
   */

  /**
   * Function to get a error response.
   * @typedef {function} OnError
   * @memberof demoWeb.Client
   * @param {number} errorCode an error code defined on Device Connect SDK For JavaScript
   * @param {string} errorMessage an error message.
   */

  /**
   * Function to get a success callback of plugin discovery.
   * @typedef {function} OnSuccessPluginDiscovery
   * @memberof demoWeb.Client
   * @param {object} json a response of GET /gotapi/system.
   */

  /**
   * Function to get a success callback of service discovery.
   * @typedef {function} OnSuccessDeviceDiscovery
   * @memberof demoWeb.Client
   * @param {object} json a result of GET /gotapi/servicediscovery
   */

  /**
   * Discovers released plugins.
   * @public
   * @memberof demoWeb.Client
   * @param {demoWeb.Client.PluginDiscoveryCallback} callback
   */
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

  /**
   * Returns a list of the last known devices.
   * @public
   * @returns an array of devices which obtained by the last device discovery.
   * @see {@link demoWeb.Client#discoverDevices}
   */
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
      for (var i = 0; i < removeServices.length; i++) {
        this.removeLastKnownDevices(removeServices[i]);
      }
    }
  }

  /**
   * Discovers devices to which GotAPI server can access currently.
   * @public
   * @memberof demoWeb.Client
   * @param {DeviceDiscoveryCallback} callback
   */
  Client.prototype.discoverDevices = function(callback) {
    var self = this;

    dConnect.discoverDevices(self.settings.accessToken, function(json) {
      self.cleanupLastKnownDevices(json.services);

      var count = 0;
      var length = json.services.length;
      if (length == 0) {
        callback.onsuccess(self.lastKnownDevices);
      } else {
        for (var i = 0; i < json.services.length; i++) {
          if (self.containLastKnownDevices(json.services[i])) {
            count++;
            if (count == length) {
              callback.onsuccess(self.lastKnownDevices);
            }
          } else {
            self.serviceInfomartion(json.services[i], function(service) {
              if (service) {
                self.lastKnownDevices.push(service);
              }
              count++;
              if (count == length) {
                callback.onsuccess(self.lastKnownDevices);
              }
            });
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

  Client.prototype.serviceInfomartion = function(service, callback) {
    var self = this;

    dConnect.getSystemDeviceInfo(service.id, self.settings.accessToken, function(status, header, responseText) {
      var json = JSON.parse(responseText);
      if (json.result == 0) {
        service['scopes'] = json.supports;
        callback(service);
      } else {
        callback(undefined);
        
      }
    }, function(errorCode, errorMessage) {
      callback(undefined);
    });
  }

  /**
   * Opens and connects to a WebSocket.
   * @public
   * @memberof demoWeb.Client
   * @param {function} callback
   */
  Client.prototype.connectWebSocket = function(callback) {
    dConnect.connectWebSocket(this.settings.sessionKey, callback);
  };

  /**
   * Checks whether a WebSocket is connected or not.
   * @public
   * @memberof demoWeb.Client
   * @returns true if a WebSocket is connected already, otherwise false
   */
  Client.prototype.isConnectedWebSocket = function() {
    return dConnect.isConnectedWebSocket();
  }

  /**
   * Closes and disconnects a WebSocket.
   * @public
   * @memberof demoWeb.Client
   */
  Client.prototype.disconnectWebSocket = function() {
    dConnect.disconnectWebSocket();
  }

  Client.prototype.addEventListener = function(req) {
    req.devices = req.devices || [];
    req.onevent = req.onevent || function() {};
    req.onsuccess = req.onsuccess || function() {};
    req.onerror = req.onerror || function() {};

    var builder = new dConnect.URIBuilder();
    if (req.profile) {
      builder.setProfile(req.profile);
    }
    if (req.interface) {
      builder.setInterface(req.interface);
    }
    if (req.attribute) {
      builder.setAttribute(req.attribute);
    }
    builder.setServiceId(req.serviceId);
    builder.setSessionKey(this.settings.sessionKey);
    builder.setAccessToken(this.settings.accessToken);
    for (var key in req.params) {
      builder.addParameter(key, req.params[key]);
    }
    dConnect.addEventListener(builder.build(), req.onevent, req.onsuccess, req.onerror);
  }

  Client.prototype.removeEventListener = function(req) {
    req.devices = req.devices || [];
    req.onsuccess = req.onsuccess || function() {};
    req.onerror = req.onerror || function() {};

    var builder = new dConnect.URIBuilder();
    if (req.profile) {
      builder.setProfile(req.profile);
    }
    if (req.interface) {
      builder.setInterface(req.interface);
    }
    if (req.attribute) {
      builder.setAttribute(req.attribute);
    }
    builder.setServiceId(req.serviceId);
    builder.setSessionKey(this.settings.sessionKey);
    builder.setAccessToken(this.settings.accessToken);
    for (var key in req.params) {
      builder.addParameter(key, req.params[key]);
    }
    dConnect.removeEventListener(builder.build(), req.onsuccess, req.onerror);
  }
  return parent;
})({});
