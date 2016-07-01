(function () {
  'use strict';

  var demoClient;

  function showErrorDialog($modal) {
    var modalInstance = $modal.open({
      templateUrl: 'error-dialog-light-select.html',
      controller: 'ModalInstanceCtrl',
      size: 'lg',
      resolve: {
        'title': function() {
          return 'エラー';
        },
        'message': function() {
          return 'ライトが一つも選択されていません。';
        }
      }
    });
    modalInstance.result.then(function (result) {
    });
  }
  
  function containLightService(lights, serviceId, lightId) {
    for (var i = 0; i < lights.length; i++) {
      var light = lights[i];
      if (light.serviceId === serviceId && light.light.lightId == lightId) {
        return true;
      }
    }
    return false;
  }

  /**
   * ライトを検索して登録する。
   */
  function discoverLights($scope, $location, lightService) {
    var oldLights = lightService.lights;
    lightService.discoverDevices(demoClient, {
      oncomplete: function(lights) {
        for (var i = 0; i < lights.length; i++) {
          var obj = lights[i];
          var serviceId = obj.serviceId;
          var lightId = obj.light.lightId;
          obj.checked = containLightService(oldLights, serviceId, lightId);
        }

        $scope.list = {
          'name'  : 'Light一覧',
          'lights' : lights
        }
        $scope.$apply();

        var $checkboxs = $('[name=list-checkbox]');
        $checkboxs.map(function(index, el) {
          el.checked = lights[index].checked;
        });
      },
      onerror: function(errorCode, errorMessage) {
        $location.path('/error/' + errorCode);
      }
    });
  }

  var SelectLightController = function($scope, $modal, $window, $location, demoWebClient, lightService) {
    demoClient = demoWebClient;

    $scope.title = '使用するライトを選択してください';
    discoverLights($scope, $location, lightService);

    $scope.settingAll = function() {
      demoClient.discoverPlugins({
        onsuccess: function(plugins) {
          $scope.$apply(function() {
            $location.path('/settings/light');
          });
        },
        onerror: function(errorCode, errorMessage) {
          $scope.$apply(function() {
            $location.path('/error/' + errorCode);
          });
        }
      });
    }
    $scope.registerAll = function() {
      $('input[name=list-checkbox]').prop("checked", true);
    }
    $scope.unregisterAll = function() {
      $('input[name=list-checkbox]').prop("checked", false);
    }
    $scope.cancel = function() {
      $window.history.back();
    }
    $scope.ok = function() {
      var $checked = $('[name=list-checkbox]:checked');
      if ($checked.length == 0) {
        showErrorDialog($modal);
      } else {
        lightService.removeAll();
        var $checkbox = $('[name=list-checkbox]');
        var valList = $checkbox.map(function(index, el) {
          if (el.checked) {
            lightService.addLight($scope.list.lights[index]);
          }
          return $scope.list.lights[index];
        });
        $window.history.back();
      }
    }
    $scope.back = function() {
      $location.path('/light');
    };
  };

  angular.module('demoweb')
    .controller('SelectLightController', 
      ['$scope', '$modal', '$window', '$location', 'demoWebClient', 'lightService', SelectLightController]);
})();
