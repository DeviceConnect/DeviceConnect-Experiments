(function () {
  'use strict';

  function showErrorDialog($modal, $location, settingsPath) {
    var modalInstance = $modal.open({
      templateUrl: 'error-dialog-device-radio.html',
      controller: 'ModalInstanceCtrl',
      size: 'lg',
      resolve: {
        'title': function() {
          return 'エラー';
        },
        'message': function() {
          return 'デバイスが接続されていません。設定画面を開きますか？';
        }
      }
    });
    modalInstance.result.then(function (result) {
      if (result) {
        $location.path(settingsPath);
      }
    });
  }

  function findSelectedIndex(selectedDevice, allDevices) {
    if (selectedDevice !== undefined) {
      for (var i = 0; i < allDevices.length; i++) {
        if (selectedDevice.id === allDevices[i].id) {
          return i;
        }
      }
    }
    return -1;
  }

  function refreshDeviceList(list, allDevices) {
    var i, newArray = [];

    for (i = 0; i < list.devices.length; i++) {
      if (findSelectedIndex(list.devices[i], allDevices) === -1) {
        delete list.devices[i];
      }
    }
    for (i = 0; i < list.devices.length; i++) {
      if (list.devices[i] !== undefined) {
        newArray.push(list.devices[i]);
      }
    }
    list.devices = newArray;
  }

  var DeviceRadioController = function ($scope, $modal, $window, $routeParams, $location, demoWebClient, deviceService) {
    var demoName = $routeParams.demoName,
        profileName = $routeParams.profileName,
        subProfileName = $routeParams.subProfileName,
        settingsPath = '/settings/' + demoName + '/' + profileName;

    var scopeName = profileName;
    if (subProfileName !== undefined) {
    	scopeName = subProfileName;
    }

    deviceService.searchDevices(demoWebClient, scopeName, function(devices) {

      refreshDeviceList(deviceService.list(demoName), devices);

      if (devices.length == 0) {
        showErrorDialog($modal, $location, settingsPath);
      } else {
        $scope.list = {
          'name'  : 'デバイス一覧',
          'devices' : devices
        }
        $scope.$apply();

        setTimeout(function() {
          var $radio = $('[name=list-radio]'),
              selectedIndex = findSelectedIndex(deviceService.list(demoName).devices[0], devices);
          if (selectedIndex === -1) {
            selectedIndex = 0;
          }
          $radio.map(function(index, el) {
            if (index === selectedIndex) {
              el.checked = true;
            }
          });
        }, 100);
      }
    });

    $scope.title = "デバイス選択";
    $scope.settingAll = function() {
      demoWebClient.discoverPlugins({
        onsuccess: function(plugins) {
          $scope.$apply(function() {
            $location.path(settingsPath);
          });
        },
        onerror: function(errorCode, errorMessage) {
          $scope.$apply(function() {
            $location.path('/error/' + errorCode);
          });
        }
      });
    }
    $scope.back = function() {
      $window.history.back();
    };
    $scope.cancel = function() {
      $window.history.back();
    }
    $scope.ok = function() {
      var $checked = $('[name=list-radio]:checked');
      if ($checked.length == 0) {
        showErrorDialog($modal, $location, settingsPath);
      } else {
        deviceService.list(demoName).removeAll();
        var $radio = $('[name=list-radio]');
        var valList = $radio.map(function(index, el) {
          if (el.checked) {
            deviceService.list(demoName).addDevice($scope.list.devices[index]);
          }
          return $scope.list.devices[index];
        });
        $window.history.back();
      }
    }
  }

  angular.module('demoweb')
    .controller('DeviceRadioController',
      ['$scope', '$modal', '$window', '$routeParams', '$location', 'demoWebClient', 'deviceService', DeviceRadioController]);
})();
