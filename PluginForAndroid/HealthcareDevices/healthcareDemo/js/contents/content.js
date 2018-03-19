(function() {

  var progressModal;

  var Version = function(versionName) {
    this.numbers = [];
    var tmp = versionName.split('.');
    for (var i = 0; i < 3; i++) {
      this.numbers[i] = Number(tmp[i]);
    }
  }
  Version.prototype.compareTo = function(other) {
    function compareNums(a, b) {
      return (a > b) ? 1 : (a == b) ? 0 : -1;
    }

    var result;
    for (var i = 0; i < this.numbers.length; i++) {
      result = compareNums(this.numbers[i], other.numbers[i]);
      if (result !== 0) {
        return result;
      }
    }
    return 0;
  }

  angular.module('demoweb')
    .controller('demoCtrl', ['$scope', '$location', '$modal', 'demoWebClient', 'demoConstants', 'transition', function($scope, $location, $modal, demoWebClient, demoConstants, transition) {
      progressModal = $modal;
      transition.scope = $scope;

      demoWebClient.checkAvailability({
        onsuccess: function(version) {
          if (isUpdateNeeded(version)) {
            showUpdatePrompt();
            return;
          }
          transition.next('/');
        },
        onerror: function(errorCode, errorMessage) {
          var path;
          switch(errorCode) {
          case -1:
            path = '/launch';
            break;
          default:
            path = '/error/' + errorCode;
            break;
          }
          transition.next(path);
        }
      });

      $scope.settingAll = function() {
        demoWebClient.discoverPlugins({
          onsuccess: function(plugins) {
            transition.next('/settings');
          },

          onerror: function(errorCode, errorMessage) {
            transition.next('/error/' + errorCode);
          }
        });
      };

      function showUpdatePrompt() {
        var uri, name;
        if (isAndroid()) {
          uri = createUriForAndroid();
          name = demoConstants.manager.android.name;
        } else if (isIOS()) {
          uri = createUriForIOS();
          name = demoConstants.manager.ios.name;
        } else {
          return;
        }
        var modalInstance = progressModal.open({
          templateUrl: 'update-prompt-dialog.html',
          controller: 'ModalInstanceCtrl',
          size: 'lg',
          resolve: {
            'title': function() {
              return '注意';
            },
            'message': function() {
              return name + 'を最新版にアップデートしてください。';
            }
          }
        });
        modalInstance.result.then(function (result) {
          if (result) {
            location.href = uri;
          }
        }); 
      }

      function isUpdateNeeded(currentVersionName) {
        var currentVersion = new Version(currentVersionName);
        var minVersion;
        if (isAndroid()) {
          minVersion = new Version(demoConstants.manager.android.minVersion);
        } else if (isIOS()) {
          minVersion = new Version(demoConstants.manager.ios.minVersion);
        } else {
          return false;
        }
        return currentVersion.compareTo(minVersion) == -1;
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

      function createUriForAndroid() {
        return 'https://play.google.com/store/apps/details?id=' + demoConstants.manager.android.packageName;
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

    }]);
})();