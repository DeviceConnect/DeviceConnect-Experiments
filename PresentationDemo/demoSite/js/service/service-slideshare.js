/**
 service-slideshare.js
 Copyright (c) 2016 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */

(function() {

  var BASE_URL = "http://www.slideshare.net/api/oembed/2?url=http://www.slideshare.net/";
  var BASE_URL_SUFFIX = "&format=jsonp";

  var totalPageNumber;
  var currentPageNumber;
  var slideList;

  function slideshareService(){
    this.updateSlide = updateSlide;
    this.getCurrentImageUrl = getCurrentImageUrl;
    this.getCurrentPageNumber = getCurrentPageNumber;
    this.getTotalPageNumber = getTotalPageNumber;
    this.first = first;
    this.last = last;
    this.prev = prev;
    this.next = next;
    this.setPageNumber = setPageNumber;
  }

  function updateSlide(userName, slideName, suffix, callback){
    var uri = BASE_URL + userName + '/' + slideName + BASE_URL_SUFFIX;
    requestJsonp(uri, function(data) {
      currentPageNumber = 1;
      totalPageNumber = data.total_slides;
      slideList = [];
      var suf = suffix ? suffix : data.slide_image_baseurl_suffix;
      for(var i = 0; i < data.total_slides; i++){
        slideList.push('https:'+ data.slide_image_baseurl + (i+1) + suf);
      }
      callback();
    });
  }

  function setPageNumber(number){
    if(number < 1){ number = 1; }
    if(number > totalPageNumber){ number = totalPageNumber; }
    currentPageNumber = number;
  }

  function getCurrentImageUrl(){ return slideList[currentPageNumber - 1]; }
  function getCurrentPageNumber(){ return currentPageNumber; }
  function getTotalPageNumber(){ return totalPageNumber; }
  function prev(){ if(currentPageNumber > 1){ currentPageNumber -= 1; } }
  function next(){ if(currentPageNumber < totalPageNumber){ currentPageNumber += 1; } }
  function first(){ currentPageNumber = 1; }
  function last(){ currentPageNumber = totalPageNumber; }

  function requestJsonp(url, callback) {
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

  angular.module('LinkingDemo').service('slideshare', ['manager' , 'store' , slideshareService]);
})();
