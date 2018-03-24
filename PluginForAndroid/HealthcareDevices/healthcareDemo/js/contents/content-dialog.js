(function() {
  angular.module('demoweb').controller('ModalInstanceCtrl', function ($scope, $modalInstance, title, message) {
    $scope.title = title;
    $scope.message = message;
    $scope.ok = function () {
      $modalInstance.close(true);
    };
    $scope.cancel = function() {
      $modalInstance.close(false);
    };
  });
})();
