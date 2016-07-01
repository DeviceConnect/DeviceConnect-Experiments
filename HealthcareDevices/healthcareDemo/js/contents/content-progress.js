(function() {
  angular.module('demoweb').controller('ProgressInstanceCtrl', function ($scope, $modalInstance, title, message) {
    $scope.title = title;
    $scope.message = message;
    $scope.cancel = function () {
      $modalInstance.dismiss('cancel');
    };
  });
})();
