(function() {

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
          return 'Device Web API Managerの起動完了を待っています...';
        }
      }
    });
    modalInstance.result.then(function () {
    }, function() {
      if (pluginTimerId) {
        clearTimeout(pluginTimerId);
        pluginTimerId = undefined;
      }
    });
    return modalInstance;
  }

  function showRetryPrompt($scope) {
    var modalInstance = progressModal.open({
      templateUrl: 'retry-prompt-dialog.html',
      controller: 'ModalInstanceCtrl',
      size: 'lg',
      resolve: {
        'title': function() {
          return '注意';
        },
        'message': function() {
          return 'Device Web APIの起動完了を確認できませんでした。再試行しますか？';
        }
      }
    });
    modalInstance.result.then(function (result) {
      if (result) {
        $scope.startManager();
      }
    });
  }

  function showConfirm() {
    var modalInstance = progressModal.open({
      templateUrl: 'confirm-dialog.html',
      controller: 'ModalInstanceCtrl',
      size: 'lg',
      resolve: {
        'title': function() {
          return '確認';
        },
        'message': function() {
          return 'Device Web APIの起動完了を確認しました。デモ一覧画面に戻ります。';
        }
      }
    });
    modalInstance.result.then(function () {
      appLocation.path('/');
    });
  }

  function isMobile() {
    return isAndroid() || isIOS();
  }

  function isAndroid() {
    var ua = navigator.userAgent;
    if (/Android/.test(ua)) {
      return true;
    }
    return false;
  }

  function isIOS() {
    var ua = navigator.userAgent;
    if(/iPhone/.test(ua)) {
      return true;
    } else if(/iPad/.test(ua)) {
      return true;
    }
    return false;
  }

  angular.module('demoweb')
    .controller('launchCtrl', ['$scope', '$location', '$window', '$modal', 'demoWebClient', 'demoConstants', function($scope, $location, $window, $modal, demoWebClient, demoConstants) {
      progressModal = $modal;
      client = demoWebClient;
      appLocation = $location;

      $scope.title = 'システム起動確認';

      if (isIOS()) {
        $scope.message = 'Device Web API Browserが端末にインストールされているかどうかを確認します。よろしいですか？<br><br>・インストール済みの場合は、Device Web API Browserが起動します。<br><br>・未インストールの場合は、App Storeへ移動します。';
      } else {
        $scope.message = 'Device Web API Managerが端末にインストールされているかどうかを確認します。よろしいですか？<br><br>・インストール済みの場合は、Device Web API Managerの起動画面が表示されます。Launchボタンで起動してください。<br><br>・未インストールの場合は、Google Playへ移動します。インストール後、端末標準のランチャーからDevice Web API Managerの設定画面を起動し、ManagerをONにしてください。';
      }

      $scope.startManager = function() {
        if (!isMobile()) {
          showWarning();
          return;
        }

        var modalInstance = showProgress();
        waitAvailability({
          onavailable: function(version) {
            modalInstance.close();
            demoWebClient.connectWebSocket(function() {});
            showConfirm();
          },
          ontimeout: function() {
            modalInstance.close();
            showRetryPrompt($scope);
          },
          onmarket: function() {
            modalInstance.close();
          }
        }, 15 * 1000);

        if (demoConstants.DEBUG && isAndroid()) {
          $window.location.href = './trial/apk/dConnectManager.apk';
        } else {
          demoWebClient.startManager();
        }
      };
      $scope.back = function() {
        $window.history.back();
      };

      function createUriForIOS() {
        return 'itms-apps://itunes.apple.com/app/id' + demoConstants.manager.ios.appId + '?ls=1&mt=8';
      }

      function showWarning() {
        console.log('showWarning: manager');
        var modalInstance = progressModal.open({
          templateUrl: 'dialog.html',
          controller: 'ModalInstanceCtrl',
          size: 'lg',
          resolve: {
            'title': function() {
              return '注意';
            },
            'message': function() {
              return 'PC上で操作する場合は、Device Web API ManagerをAndroid端末上にインストールかつ起動した後、本ページをリロードしてください。';
            }
          }
        });
        modalInstance.result.then(function (result) {
          if (result) {
            var url = createUriForAndroid();
            console.log('Google Play: ' + url);
            $window.location.href = url;
          }
        });
        return modalInstance;
      }

      function waitAvailability(callback, timeout) {
        var interval = 250; // msec
        if (timeout <= 0) {
          callback.ontimeout();
          return;
        }
        pluginTimerId = setTimeout(function() {
          client.checkAvailability({
            onsuccess: function(version) {
              callback.onavailable(version);
            },
            onerror: function(errorCode, errorMessage) {
              switch (errorCode) {
                case dConnect.constants.ErrorCode.ACCESS_FAILED:
                  if (isIOS()) {
                    setTimeout(function() {
                      location.href = createUriForIOS();
                    }, 250);
                    callback.onmarket();
                    return;
                  }
                  break;
                default:
                  break;
              }
              waitAvailability(callback, timeout - interval);
            }
          });
        }, interval);
      }

    }]);
})();