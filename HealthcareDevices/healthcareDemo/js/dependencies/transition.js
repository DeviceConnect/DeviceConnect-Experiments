(function() {
  angular.module('demoweb')
    .service('transition', ['$location', function($location) {
      function next(path) {
        this.scope.$apply(function() {
          $location.path(path);
        });
      }
      
      var service = {
        next: next
      };
      return service;
    }]);
})();
