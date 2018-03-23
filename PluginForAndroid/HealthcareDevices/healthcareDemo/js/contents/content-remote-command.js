(function () {
  'use strict';

  var commandList = [];

  function saveCommandList(service) {
    store.set(service.id, commandList);
  }

  function addCommand(name, message) {
    commandList.push({name: name, message: message});
  }

  function getCommand($modal, client, service) {
    client.request({
      "method": "GET",
      "profile": "remote_controller",
      "devices": [service.id],
      "params": {},
      "onsuccess": function(id, json) {
        if (json.message && json.message.length > 0) {
          openDialog($modal, json.message, service);
        } else {
          openNoDataDialog($modal);
        }
      },
      "onerror": function(errorCode, errorMessage) {
        openNoDataDialog($modal);
      }
    });
  }

  function openDialog($modal, message, service) {
    var modalInstance = $modal.open({
      templateUrl: 'add-dialog-remoconn.html',
      controller: 'ModalInstanceCtrl',
      size: 'lg',
      resolve: {
        'title': function() {
          return 'コマンド追加';
        },
        'message': function() {
          return message;
        }
      }
    });
    modalInstance.result.then(function (result) {
      if (result) {
        var name = $("#commandName").val();
        if (name.length == 0) {
          openErrorDialog($modal, 'エラー', 'コマンド名が入力されていません。');
          return;
        }

        var message = $("#commandMessage").val();
        if (message.length == 0) {
          openNoDataDialog($modal);
          return;
        }
        addCommand(name, message);
        saveCommandList(service);
      }
    });
  }

  function openNoDataDialog($modal) {
    openErrorDialog($modal, 'エラー', 'コマンドメッセージが入力されていません。IRKitに赤外線リモコンでデータを送信してから、GETボタンを押下してください。');
  }

  function openErrorDialog($modal, title, message) {
    var modalInstance = $modal.open({
      templateUrl: 'error-dialog-remoconn.html',
      controller: 'ModalInstanceCtrl',
      size: 'lg',
      resolve: {
        'title': function() {
          return title;
        },
        'message': function() {
          return message;
        }
      }
    });
    modalInstance.result.then(function (result) {
    });
  }
  
  function openRemoveDialog($modal, service, index) {
    var modalInstance = $modal.open({
      templateUrl: 'confirm-dialog-remoconn.html',
      controller: 'ModalInstanceCtrl',
      size: 'lg',
      resolve: {
        'title': function() {
          return 'コマンド削除';
        },
        'message': function() {
          return '「' + commandList[index].name + '」を削除して良いですか？';
        }
      }
    });
    modalInstance.result.then(function (result) {
      if (result) {
        commandList.splice(index, 1);
        saveCommandList(service);
      }
    });
  }

  var RemoteCommandController = function ($scope, $modal, $window, $location, demoWebClient, deviceService) {
    var devices = deviceService.list('remote').devices;

    if (devices && devices.length > 0) {
      commandList = store.get(devices[0].id);
      if (commandList == undefined) {
        commandList = [];
      }
    } else {
      openErrorDialog($modal, 'エラー', 'デバイスが選択されていません。');
    }

    $scope.title = 'コマンド管理';
    $scope.list = {
      'commands' : commandList
    }
    $scope.settingAll = function() {
      demoWebClient.discoverPlugins({
        onsuccess: function(plugins) {
          $scope.$apply(function() {
            $location.path('/settings/remote');
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
      $location.path('/remote/controller');
    };
    $scope.removeCommand = function(index) {
      openRemoveDialog($modal, devices[0], index);
    };
    $scope.addCommand = function() {
      getCommand($modal, demoWebClient, devices[0]);
    };
  };

  angular.module('demoweb')
    .controller('RemoteCommandController', 
      ['$scope', '$modal', '$window', '$location', 'demoWebClient', 'deviceService', RemoteCommandController]);
})();
