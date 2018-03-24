(function() {
  angular.module('demoweb')
    .controller('promptSettingsCtrl', ['$scope', '$routeParams', '$window', '$location', 'demoWebClient', 'demoConstants', function($scope, $routeParams, $window, $location, demoWebClient, demoConstants) {
      console.log('prompt: demoName=' + $routeParams.demoName);

      var demoName = $routeParams.demoName;
      if (!demoName) {
        return;
      }

      $scope.title = '準備';
      var plugins = demoWebClient.getPlugins({ installed: true, profiles: demoConstants.demos[demoName].profiles });
      console.log('plugins: ', plugins);
      if (plugins.length === 0) {
        $scope.message = '必要なプラグインがインストールされていません。インストールしますか？';
      } else {
        $scope.message = '必要なデバイスが未接続です。プラグインの設定画面で接続設定を行いますか？';
      }

      $scope.next = function() {
        $location.path('/settings/' + demoName);
      };
      $scope.back = function() {
        $window.history.back();
      };
    }]);
})();