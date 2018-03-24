(function() {


  /**
   * Get a ip string.
   * @return ip
   */
  function getIpString() {
    if (1 < document.location.search.length) {
      var query = document.location.search.substring(1);
      var parameters = query.split('&');
      for (var i = 0; i < parameters.length; i++) {
        var element = parameters[i].split('=');
        var paramName = decodeURIComponent(element[0]);
        var paramValue = decodeURIComponent(element[1]);
        if (paramName == 'ip') {
          return paramValue;
        }
      }
    }
    return 'localhost';
  }

  function isAndroid() {
    return (navigator.userAgent.indexOf('Android') > 0);
  }

  angular.module('demoweb')
    .factory('demoWebClient', ['demoConstants', function (demoConstants) {
      var client = new demoWeb.Client(getIpString());
      client.setApplicationName(demoConstants.applicationName);
      client.setScopes(demoConstants.scopes);
      if (isAndroid()) {
        client.setReleasedPlugins(demoConstants.plugins);
      }
      return client;
    }]);
})();