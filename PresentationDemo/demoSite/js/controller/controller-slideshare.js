/**
 controller-slideshare.js
 Copyright (c) 2016 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */

(function() {

  function controller($scope, $location){
    init();

    setTimeout(function(){
      console.log('log');
      var currentPage = $('#iframe').contents().find('#current-slide');
      var btnPrevious = $('#iframe').contents().find('#btnPrevious');
      // var btnNext = $('#btnNext');
      console.log(btnPrevious);
      // console.log(btnNext);
      console.log(currentPage);
      console.log(currentPage.text());
      console.log(currentPage.text());
      btnPrevious.click(function(){
        console.log('keyup');
      });
    },1000);

    function init(){
      setValuesToScope();
      setEventsToScope();
      setFunctionsToScope();
      // getSlideShare();
    }

    function setValuesToScope(){
      $scope.pageNumber = 0;
    }

    function setEventsToScope(){
    }

    function setFunctionsToScope(){
    }

    function getSlideShare(){
      var uri = 'http://www.slideshare.net/api/oembed/2?url=http://www.slideshare.net/haraldf/business-quotes-for-2011&format=jsonp';
      jsonp(uri, function(data) {
         console.log(data);
         var html = data.html;
         console.log(html);
        //  alert(data);
        //https:<slide_image_baseurlパラメータの値><ページ番号><slide_image_baseurl_suffixパラメータの値>
        // var slides = [];
        // var count = data.total_slides;
        // for(var i = 0; i < count; i++){
        //   slides.push('https:'+ data.slide_image_baseurl + (i+1) + data.slide_image_baseurl_suffix);
        // }
        // $scope.$apply(function(){
        //   $scope.slides = slides;
        // });
      });
    }

    function jsonp(url, callback) {
      var callbackName = 'jsonp_callback_' + Math.round(100000 * Math.random());
      window[callbackName] = function(data) {
          delete window[callbackName];
          document.body.removeChild(script);
          callback(data);
      };

      var script = document.createElement('script');
      script.src = url + (url.indexOf('?') >= 0 ? '&' : '?') + 'callback=' + callbackName;
      document.body.appendChild(script);
    }



  }

  angular.module('LinkingDemo').controller('SlideShareController',
  ['$scope', '$location', controller]);
})();
