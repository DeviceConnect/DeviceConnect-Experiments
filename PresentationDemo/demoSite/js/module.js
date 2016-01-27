/**
 module.js
 Copyright (c) 2016 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */

angular.module('LinkingDemo', ['ngRoute', 'ngSanitize', 'ngTouch', 'angularSpinner', 'ui.bootstrap'])
  .config(['$routeProvider', function($routeProvider) {
    $routeProvider
    .when('/', {
      templateUrl: 'html/top.html',
      controller: 'TopController'
    })
    .when('/slideshare', {
      templateUrl: 'html/slideshare.html',
      controller: 'SlideShareController'
    })
    .otherwise({redirectTo: '/'});
  }])
  .directive('imgLoad',['$parse',function($parse){
    return {
      restrict: 'A',
      link: function(scope, elem, attrs){
        var fn = $parse(attrs.imgLoad);
        elem.on('load',function(event){
          scope.$apply(function(){
            fn(scope, {$event:event});
          });
        });
      }
    };
  }]);
