(function() {

  var _skywayApiKey = "[ YOUR API KEY ]";
  var _skywayDomain = "[ YOUR DOMAIN ]";

  var _ip;
  var _accessToken;
  var _serviceId;
  var _sessionKey = Math.random().toString(36).slice(-8);

  function createConfig() {
    return '{apiKey:"' + _skywayApiKey + '", domain:"' + _skywayDomain + '"}';
  }

  function createUriBuilder(attribute) {
    var builder = new dConnect.URIBuilder();
    builder.setProfile('videochat');
    builder.setServiceId(_serviceId);
    builder.setAccessToken(_accessToken);
    builder.setAttribute(attribute);
    builder.addParameter('config', createConfig());
    return builder;
  }

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

  function getCookie(name) {
    var result = null;
    var cookieName = name + '=';
    var allcookies = document.cookie;
    var position = allcookies.indexOf(cookieName);
    if (position != -1) {
      var startIndex = position + cookieName.length;
      var endIndex = allcookies.indexOf(';', startIndex);
      if (endIndex == -1) {
        endIndex = allcookies.length;
      }
      result = decodeURIComponent(allcookies.substring(startIndex, endIndex));
    }
    return result;
  }

  function authorization() {
    var scopes = Array(
        'servicediscovery',
        'serviceinformation',
        'system',
        'videochat');

    dConnect.authorization(scopes, 'WebRTC Processing Demo',
        function(clientId, accessToken) {
          _accessToken = accessToken;
          console.log(clientId + " " + accessToken);
          document.cookie = 'accessToken' + _ip + '=' + accessToken;
          searchWebRTCDevicePlugin();
        },
        function(errorCode, errorMessage) {
          showErrorDialog("Error", "Failed to the authroization.<br>errorCode: " + errorCode + "<br>" + errorMessage);
        });
  }

  function openWebsocket() {
    if (!dConnect.isConnectedWebSocket()) {
      dConnect.connectWebSocket(_sessionKey, function(type, message) {
        console.log('websocket: ' + type + ", " + message);
      });
    } else {
      console.log('WebSocket has opened already.');
    }
  }

  function searchWebRTCDevicePlugin() {
    dConnect.discoverDevices(_accessToken, function(obj) {
      var result = false;
      for (var i = 0; i < obj.services.length; i++) {
        if (obj.services[i].name == 'WebRTC Service') {
          _serviceId = obj.services[i].id;
          result = true;
        }
      }
      if (!result) {
        showErrorDialog("Error", "WebRTC Service not found.");
      } else {
        getMyProfile();
      }
    }, function(errorCode, errorMessage) {
      showErrorDialog("Error", "Failed to discovery the plug-in.<br>errorCode: " + errorCode + "<br>" + errorMessage);
    });
  }

  function getMyProfile() {
    var builder = createUriBuilder('profile');
    var successCallback = function (json) {
      $('#skyway-id').val(json.addressId);
      $('#my .skyway-id').text(json.addressId);
      registerEvent();
      registerOnCallEvent();
      registerHangupEvent();
    };
    var errorCallback = function (errorCode, errorMessage) {
      showErrorDialog("Error", "Failed to connect the skyway.<br>errorCode: " + errorCode + "<br>" + errorMessage);
    };
    dConnect.get(builder.build(), null, successCallback, errorCallback);
  }

  function getAddressList() {
    var builder = createUriBuilder('address');
    dConnect.get(builder.build(), null, function(json) {
      var peers = json.addresses;
      var myId = $('#skyway-id').val();
      var str = '';
      for (var i = 0; i < peers.length; i++) {
        var peerId = peers[i].addressId;
        if (myId === peerId) { continue; }
        str += '<li class="list-group-item">' + peerId + '</li>';
      }
      var listview = $('#collees-list');
      listview.empty();
      listview.append(str);
      var height = Math.min(200, (peers.length * 42));
      listview.height(height);
      $('#collees-list > li').on('click', function() {
        $('#input-callee-id').val($(this).text());
      });
    }, function(errorCode, errorMessage) {
      showErrorDialog("Error", "Failed to get the list.<br>errorCode: " + errorCode + "<br>" + errorMessage);
    });
  }

  function makeCall(outputs) {
    var addressId = $('#input-callee-id').val();
    var videoUrl = $('#video-url').val();
    var audioUrl = $('#audio-url').val();
    var samplingRate = $('#audio-sampling-rate').val();
    var bitDepth = $('#audio-bit-rate').val();
    var channel = $('#audio-channel').val();
    
    console.log("samplingRate=" + samplingRate);
    console.log("bitDepth=" + bitDepth);
    console.log("channel=" + channel);
    
    var builder = createUriBuilder('call');
    builder.addParameter('addressId', addressId);
    builder.addParameter('video', videoUrl);
    builder.addParameter('audio', audioUrl);
    builder.addParameter('audioSampleRate', samplingRate);
    builder.addParameter('audioBitDepth', bitDepth);
    builder.addParameter('audioChannel', channel);
    builder.addParameter('outputs', outputs);
    var successCallback = function(json) {
      console.log("Success to make call.");
    };
    var errorCallback = function(errorCode, errorMessage) {
      showErrorDialog("Error", "Failed to make call.<br>errorCode: " + errorCode + "<br>" + errorMessage);
    };
    dConnect.post(builder.build(), null, null, successCallback, errorCallback);
  }

  function endCall() {
    var addressId = $('#input-callee-id').val();
    var builder = createUriBuilder('call');
    builder.addParameter('addressId', addressId);
    var successCallback = function(json) {
        console.log("Success to end call.");
    };
    var errorCallback = function(errorCode, errorMessage) {
      showErrorDialog("Error", "Failed to end call.<br>errorCode: " + errorCode + "<br>" + errorMessage);
    };
    dConnect.delete(builder.build(), null, successCallback, errorCallback);
  }

  function answer() {
    var addressId = $('#input-callee-id').val();
    var videoUrl = $('#video-url').val();
    var audioUrl = $('#audio-url').val();
    var builder = createUriBuilder('call');
    builder.addParameter('addressId', addressId);
    builder.addParameter('video', videoUrl);
    builder.addParameter('audio', audioUrl);
    var successCallback = function(json) {
      console.log("Success to snswer.");
    };
    var errorCallback = function(errorCode, errorMessage) {
      showErrorDialog("Error", "Failed to answer.<br>errorCode: " + errorCode + "<br>" + errorMessage);
    };
    dConnect.post(builder.build(), null, null, successCallback, errorCallback);
  }

  function registerEvent() {
    var builder = createUriBuilder('incoming');
    builder.addParameter('sessionKey', _sessionKey);
    var eventCallback = function(message) {
      console.log('Event-Message:' + message);
      var json = JSON.parse(message);
      if (json.incoming) {
          $('#input-callee-id').val(json.incoming.addressId);
          $('.modal-title').text("Calling");
          $('.modal-body').html("<h3>" + json.incoming.addressId + "</h3> incoming.");
          $('#call-dialog').modal('show');
      }
    };
    var successCallback = function(json) {
      console.log('Success to register event.');
    };
    var errorCallback = function(errorCode, errorMessage) {
      showErrorDialog("Error", "Failed to register event.<br>errorCode: " + errorCode + "<br>" + errorMessage);
    };
    dConnect.addEventListener(builder.build(), eventCallback, successCallback, errorCallback);
  }

  function registerOnCallEvent() {
    var builder = createUriBuilder('oncall');
    builder.addParameter('sessionKey', _sessionKey);
    var eventCallback = function(message) {
      console.log('Event-Message:' + message);
      var json = JSON.parse(message);
      if (json.oncall) {
        var local = json.oncall[0].local;
        var remote = json.oncall[0].remote;
        $('#other .skyway-id').text(json.oncall[0].addressId);
        if (local) {
          if (local.video) {
            var uri = local.video.uri;
            if (uri) {
               $('#local-video').attr("src", uri);
            }
          }
          if (local.audio) {
            var uri = local.audio.uri.replace('http', 'ws');
            var samplingRate = local.audio.sampleRate;
            var channels = local.audio.channels;
            var sampleSize = local.audio.sampleSize;
            var audioFormat = AudioUtil.AudioDevice.PCM_FLOAT;
            if (sampleSize == 8) {
                audioFormat = AudioUtil.AudioDevice.PCM_8BIT;
            } else if (sampleSize == 16) {
                audioFormat = AudioUtil.AudioDevice.PCM_16BIT;
            }
            var audio = new AudioUtil.AudioDevice();
            audio.url(uri)
              .channel(channels)
              .sampleRate(samplingRate)
              .audioFormat(audioFormat)
              .onopen(function() {
                    console.log("open local audio. uri=" + uri);
                }).onerror(function() {
                    console.log("error local audio");
                }).onclose(function() {
                    console.log("close local audio");
                }).connect();
          }
        }

        if (remote) {
          if (remote.video) {
              var uri = remote.video.uri;
              if (uri) {
                 $('#remote-video').show();
                 $('#remote-video').attr("src", uri);
              }
          }
          if (remote.audio) {
              var uri = remote.audio.uri.replace('http', 'ws');
              var samplingRate = remote.audio.sampleRate;
              var channels = remote.audio.channels;
              var sampleSize = remote.audio.sampleSize;
              var audioFormat = AudioUtil.AudioDevice.PCM_FLOAT;
              if (sampleSize == 8) {
                  audioFormat = AudioUtil.AudioDevice.PCM_8BIT;
              } else if (sampleSize == 16) {
                  audioFormat = AudioUtil.AudioDevice.PCM_16BIT;
              }
              var audio = new AudioUtil.AudioDevice();
              audio.url(uri)
                .channel(channels)
                .sampleRate(samplingRate)
                .audioFormat(audioFormat)
                .onopen(function() {
                    console.log("open remote audio. uri=" + uri);
                  }).onerror(function() {
                    console.log("error remote audio");
                  }).onclose(function() {
                    console.log("close remote audio");
                  }).connect();
            }
        }
      }
    };
    var successCallback = function(json) {
      console.log('Success to register event.');
    };
    var errorCallback = function(errorCode, errorMessage) {
      showErrorDialog("Error", "Failed to register event.<br>errorCode: " + errorCode + "<br>" + errorMessage);
    };
    dConnect.addEventListener(builder.build(), eventCallback, successCallback, errorCallback);
  }

  function registerHangupEvent() {
    var builder = createUriBuilder('hangup');
    builder.addParameter('sessionKey', _sessionKey);
    var eventCallback = function(message) {
      console.log('Event-Message:' + message);
      $('#other .skyway-id').text("XXXXXXXX");
      $('#remote-video').hide();
    };
    var successCallback = function(json) {
      console.log('Success to register event.');
    };
    var errorCallback = function(errorCode, errorMessage) {
      showErrorDialog("Error", "Failed to register event.<br>errorCode: " + errorCode + "<br>" + errorMessage);
    };
    dConnect.addEventListener(builder.build(), eventCallback, successCallback, errorCallback);
  }

  function showErrorDialog(title, message) {
    $('.modal-title').text(title);
    $('.modal-body').html(message);
    $('#error-dialog').modal('show');
  }

  $(document).ready(function() {
    _ip = getIpString();
    _accessToken = getCookie('accessToken' + _ip);

    dConnect.setHost(_ip);
    if (_accessToken) {
      searchWebRTCDevicePlugin();
    } else {
      authorization();
    }

    $('#skyway-connect').on('click', function() {
      getMyProfile();
    });

    $('#local-oauth').on('click', function() {
      authorization();
    });

    $('#get-list').on('click',function() {
      getAddressList();
    });

    $('#make-call-app').on('click',function() {
      makeCall('app');
    });

    $('#make-call-host').on('click',function() {
      makeCall('host');
    });

    $('#end-call').on('click',function() {
      endCall();
    });
    
    $('#call-answer').on('click', function() {
      answer();
    });

    openWebsocket();
  });
})();