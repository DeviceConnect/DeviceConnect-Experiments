(function() {
  function errorMessage(errorCode) {
    switch(errorCode) {
    case dConnect.constants.ErrorCode.ACCESS_FAILED:
      return 'Device Web APIサーバが見つかりませんでした。';
    case dConnect.constants.ErrorCode.NOT_SUPPORT_PROFILE:
    case dConnect.constants.ErrorCode.NOT_SUPPORT_ACTION:
      return '現在サポートされていない機能です。必要なプラグインがインストールされているかをご確認ください。';
    case dConnect.constants.ErrorCode.NOT_FOUND_SERVICE:
      return '指定されたデバイスを発見できませんでした。';
    case dConnect.constants.ErrorCode.TIMEOUT:
      return 'Device Web APIサーバとの通信がタイムアウトしました。';
    case dConnect.constants.ErrorCode.INVALID_ORIGIN:
      return 'Device Web APIへのアクセスが許可されませんでした。許可リストの設定をご確認ください。';
    case dConnect.constants.ErrorCode.ILLEGAL_DEVICE_STATE:
      return 'デバイス側の状態異常が発生しました。';
    case dConnect.constants.ErrorCode.ILLEGAL_SERVER_STATE:
      return 'プラグイン側の状態異常が発生しました。';
      
    // 下記のエラーが発生した場合はアプリケーションの実装を確認する
    case dConnect.constants.ErrorCode.INVALID_SERVER:
    case dConnect.constants.ErrorCode.UNKNOWN:
    case dConnect.constants.ErrorCode.NOT_SUPPORT_ATTRIBUTE:
    case dConnect.constants.ErrorCode.EMPTY_SERVICE_ID:
    case dConnect.constants.ErrorCode.UNKNOWN_ATTRIBUTE:
    case dConnect.constants.ErrorCode.INVALID_REQUEST_PARAMETER:
    case dConnect.constants.ErrorCode.AUTHORIZATION:
    case dConnect.constants.ErrorCode.EMPTY_ACCESS_TOKEN:
    case dConnect.constants.ErrorCode.SCOPE:
    case dConnect.constants.ErrorCode.NOT_FOUND_CLIENT_ID:
    default:
      return 'Device Web APIの実行に失敗しました。';
    }
  }

  angular.module('demoweb')
    .controller('errorCtrl', ['$scope', '$routeParams', '$window', 'demoConstants', function($scope, $routeParams, $window, demoConstants) {
      $scope.title = 'エラーが発生しました';
      $scope.errorCode = $routeParams.errorCode;
      $scope.errorMessage = errorMessage(Number($routeParams.errorCode));
      
      $scope.back = function() {
        $window.history.back();
      }
    }]);
})();