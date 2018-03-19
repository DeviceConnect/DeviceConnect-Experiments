(function() {

  var apkList = {};
  apkList['org.deviceconnect.android.deviceplugin.hue'] = {
    file: 'dConnectDeviceHue.apk',
    name: 'hue'
  };
  apkList['org.deviceconnect.android.deviceplugin.sphero'] = {
    file: 'dConnectDeviceSphero.apk',
    name: 'Sphero'
  };
  apkList['org.deviceconnect.android.deviceplugin.health'] = {
    file: 'dConnectDeviceHealth.apk',
    name: 'Health'
  };
  apkList['org.deviceconnect.android.deviceplugin.hvc'] = {
    file: 'dConnectDeviceHVC.apk',
    name: 'HVC'
  };
  apkList['org.deviceconnect.android.deviceplugin.irkit'] = {
      file: 'dConnectDeviceIRKit.apk',
      name: 'IRKit'
  }

  var progressModal;

  var client;

  var appLocation;

  var pluginTimerId;

  function showProgress() {
    var modalInstance = progressModal.open({
      templateUrl: 'progress.html',
      controller: 'ProgressInstanceCtrl',
      size: 'lg',
      resolve: {
        'title': function() {
          return '待機中';
        },
        'message': function() {
          return 'インストール完了を待っています...';
        }
      }
    });
    modalInstance.result.then(function () {
    }, function() {
      if (pluginTimerId) {
        clearTimeout(pluginTimerId);
      }
    });
    return modalInstance;
  }

  function waitPlugin(packageName, modalInstance, callback) {
    console.log('waitPlugin: packageName=' + packageName);
    pluginTimerId = setTimeout(function() {
      client.discoverPlugins({
        onsuccess: function(plugins) {
          plugins = plugins.filter(function(p) {
            return p.installed && p.packageName === packageName;
          });
          if (plugins.length <= 0) {
            waitPlugin(packageName, modalInstance, callback);
          } else {
            callback.oninstalled(plugins[0]);
            modalInstance.close();
          }
          console.log('waitPlugin discoverPlugins onsuccess: ' + plugins.length);
        },
        onerror: function(errorCode, errorMessage) {
          console.log('waitPlugin: errorCode=' + errorCode + ' errorMessage=' + errorMessage);
          modalInstance.dismiss('cancel');
          appLocation.path('/error/' + errorCode);
        }
      });
    }, 250);
  }

  function isMobile() {
    var ua = navigator.userAgent;
    if(/iPhone/.test(ua)) {
      return true;
    } else if(/iPad/.test(ua)) {
      return true;
    } else if (/Android/.test(ua)) {
      return true;
    }
    return false;
  }

  angular.module('demoweb')
    .controller('settingsCtrl', ['$scope', '$window', '$routeParams', '$location', '$modal', 'demoWebClient', 'demoConstants', 'transition', function($scope, $window, $routeParams, $location, $modal, demoWebClient, demoConstants, transition) {
      progressModal = $modal;
      client = demoWebClient;
      appLocation = $location;
      transition.scope = $scope;

      $scope.title = 'デバイス設定一覧';
      var demoName = $routeParams.demoName,
          profileName = $routeParams.profileName,
          profiles, i, p;

      if (profileName) {
        profiles = [ profileName ];
      } else if (demoName) {
        profiles = demoConstants.demos[demoName].profiles;
      }

      console.log('settings demoName: ' + demoName);

      $scope.plugins = getPlugins(profiles);
      $scope.wakeup = function(index) {
        var p = $scope.plugins[index];
        if (p.installed === true) {
          openSettingWindow(p);
        } else {
          if (!isMobile()) {
            showWarning(p);
            return;
          }
          var modalInstance = showProgress();
          waitPlugin(p.packageName, modalInstance, {
              oninstalled: function(p) {
                $scope.$apply(function() {
                  $scope.plugins = getPlugins(profiles);
                });
                openSettingWindow(p);
              }
          });

          if (demoConstants.DEBUG) {
            $window.location.href = './trial/apk/' + apkList[p.packageName].file;
          } else {
            $window.location.href = 'market://details?id=' + p.packageName;
          }
        }
      };

      $scope.back = function() {
        $window.history.back();
      };
      $scope.next = function() {
        $location.path(demoConstants.demos[demoName].path);
      };

      function getPlugins(profiles) {
        var plugins;
        if (profiles) {
          plugins = demoWebClient.getPlugins({profiles: profiles});
        } else {
          plugins = demoWebClient.getPlugins();
        }
        for (i = 0; i < plugins.length; i++) {
          p = plugins[i];
          p.operation = (p.installed === true) ? '設定' : 'インストール';
        }
        return plugins;
      }

      function openSettingWindow(plugin) {
        demoWebClient.openSettingWindow({
          pluginId: plugin.id,
          onsuccess: function(json) {
            console.log('openSettingWindow: success: ', json);
          },
          onerror: function(errorCode, errorMessage) {
            transition.next('/error/' + errorCode);
          }
        });
      }

      function showWarning(plugin) {
        console.log('showWarning: ' + plugin.packageName);
        var modalInstance = progressModal.open({
          templateUrl: 'dialog.html',
          controller: 'ModalInstanceCtrl',
          size: 'lg',
          resolve: {
            'title': function() {
              return '注意';
            },
            'message': function() {
              return 'プラグインのインストールはAndroid端末上で実行する必要があります。';
            }
          }
        });
        modalInstance.result.then(function (result) {
          if (result) {
            var url = 'https://play.google.com/store/apps/details?id=' + plugin.packageName;
            console.log('Google Play: ' + url);
            $window.location.href = url;
          }
        });
        return modalInstance;
      }
    }]);
})();