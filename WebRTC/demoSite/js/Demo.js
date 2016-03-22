/**
 Demo.js
 Copyright (c) 2015 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */

var skywayId = 'your-skyway-id';

var webrtcManager;
var mystream;

var audioCtx = new AudioContext();
var soundSource;
var processedAnalyser;
var currentNode;
var destination;
var myVideoProcess;

$(function(){
  var callback = {
    onopen : function(myPeerId){
      $('#my .skyway-id').text(myPeerId);
    },
    ongetpeerids : function(peerIds){
      var myId = $('#my .skyway-id').text();
      var str = '';
      for (var i = 0; i < peerIds.length; i++) {
        var peerId = peerIds[i];
        if(myId === peerId){ continue; }
        str += '<li class="list-group-item">'+peerId+'</li>';
      }
      var listview = $('#collees-list');
      listview.empty();
      listview.append(str);
      var height = Math.min(200, ((peerIds.length - 1) * 42));
      listview.height(height);
      $('#collees-list > li').on('click', function(){
        $('#input-callee-id').val($(this).text());
      });
    },
    incoming : function(calleePeerId){
      if(typeof mystream === 'undefined'){
        return;
      }
      webrtcManager.answer(mystream);
    },
    ongetstream : function(calleePeerId, stream){
      $('#other .skyway-id').text(calleePeerId);
      // setupOtherAnalyser(stream);
      showOtherVideo(stream);
    },
    onclose : function() {
        $('#other .skyway-id').text('XXXXXX');
        $('#other video').hide();
    }
  };
  webrtcManager = new WebRTCManager(callback);
  webrtcManager.connectServer(skywayId);

  $('#get-list').on('click',function(){
    webrtcManager.getPeers();
  });
  $('#make-call').on('click',function(){
    webrtcManager.startCall($('#input-callee-id').val(), mystream);
  });
  $('#end-call').on('click',function(){
    $('#other .skyway-id').text('XXXXXX');
    $('#other video').hide();
    webrtcManager.stop();
  });
  $('.nav-pills').on("shown.bs.tab", function (e) {
      var target = e.target;
      var previous = e.relatedTarget;
      $(target.parentElement).addClass('active');
      $(previous.parentElement).removeClass('active');
      if($(target).is('#standalone')){
        switchToStandalone();
      } else if($(target).is('#peer2peer')){
        switchToPeer2Peer();
      } else {
        console.error('unknown nav button');
      }
  });

  setupVideoProcess();
  setupAudioProcess();
  showMyVideo();
});

function switchToStandalone(){
  $('#end-call').trigger('click');
  $('#other').css('display','none');
  $('.connector-btn').css('display','none');
  $('.controller.connect').css('display','none');
  $('.controller.video .other-text').css('display','none');
  $('.controller.video .other-selector').css('display','none');

  var videoArea = $('#my .video-area');
  videoArea.width(266).height(200);
  videoArea.children().width(266).height(200);
  myVideoProcess.resize();

  processedAnalyser.getAnalyser().disconnect();
  processedAnalyser.getAnalyser().connect(audioCtx.destination);

  webrtcManager.destroy();
  $('#my .skyway-id').text('XXXXXX');

  var ua = navigator.userAgent.toLowerCase();
  if(ua.indexOf('firefox') != -1){
    //When standalone mode, processing ui to enable.
    $('.collapse-btn.audio').css('display','block');
    $('.controller.audio').removeAttr('style');

    $('#my .audio-area').css('display','block');
    $('#my .audiotext').css('display','block');

    //firefox needs retake usermedia for processing audio.
    navigator.getUserMedia({audio: false, video: true}, function(stream){
      mystream = stream;
      $('#my video').prop('src', URL.createObjectURL(stream));
      navigator.getUserMedia({audio: true, video: false}, function(stream){
        soundSource = audioCtx.createMediaStreamSource(stream);
        soundSource.connect(destination);
        refreshNodes();
      }, function(){ alert("error!"); });
    }, function(){ alert("error!"); });
  }

}

function switchToPeer2Peer(){
  $('#other').css('display','block');
  $('.connector-btn').css('display','block');
  $('.controller.connect').removeAttr('style');
  $('.controller.video .other-text').css('display','block');
  $('.controller.video .other-selector').css('display','block');

  var videoArea = $('#my .video-area');
  videoArea.width(133).height(100);
  videoArea.children().width(133).height(100);
  myVideoProcess.resize();

  processedAnalyser.getAnalyser().disconnect();
  processedAnalyser.getAnalyser().connect(destination);

  webrtcManager.connectServer(skywayId);

  var ua = navigator.userAgent.toLowerCase();
  if(ua.indexOf('firefox') != -1){
    //When peer2peer mode, firefox can't send processed audio. So none.
    $('.collapse-btn.audio').css('display','none');
    $('.controller.audio').css('display','none');
    $('#my .audio-area').css('display','none');
    $('#my .audiotext').css('display','none');

    //firefox needs retake usermedia for webrtc;
    navigator.getUserMedia({audio: true, video: true}, function(stream){
      mystream = stream;
      refreshNodes();
      $('#my video').prop('src', URL.createObjectURL(stream));
    }, function(){ alert("error!"); });
  }

}

function showMyVideo () {
  navigator.getUserMedia = navigator.getUserMedia || navigator.webkitGetUserMedia || navigator.mozGetUserMedia;

  var ua = navigator.userAgent.toLowerCase();
  if(ua.indexOf('firefox') != -1){
    //When peer2peer mode, firefox can't send processed audio. So none.
    $('.collapse-btn.audio').css('display','none');
    $('.controller.audio').css('display','none');
    $('#my .audio-area').css('display','none');
    $('#my .audiotext').css('display','none');

    //firefox not support MediaStream.addTrack. So audio & video is specified true.
    navigator.getUserMedia({audio: true, video: true}, function(stream){
      mystream = stream;
      $('#my video').prop('src', URL.createObjectURL(stream));
    }, function(){ alert("error!"); });
    return;
  }

  navigator.getUserMedia({audio: false, video: true}, function(stream){
    mystream = stream;
    $('#my video').prop('src', URL.createObjectURL(stream));
    navigator.getUserMedia({audio: true, video: false}, function(stream){
      soundSource = audioCtx.createMediaStreamSource(stream);
      soundSource.connect(destination);
      mystream.addTrack(destination.stream.getAudioTracks()[0]);
      refreshNodes();
    }, function(){ alert("error!"); });
  }, function(){ alert("error!"); });
}

function showOtherVideo (stream) {
  $('#other video').show();
  $('#other video').prop('src', URL.createObjectURL(stream));
}

function setupVideoProcess(){
  var createManager = function(){
    var manager = new ImageProcessManager();
    manager.putProcessor('outline', new OutlinePatch());
    manager.putProcessor('outline_white', new OutlineWhitePatch());
    manager.putProcessor('reverse', new ReversePatch());
    manager.putProcessor('grayscale', new GrayScalePatch());
    manager.putProcessor('mosaic', new MosaicPatch());
    manager.putProcessor('comic', new ComicPatch());
    //and more...

    return manager;
  };
  var myManager = createManager();
  myManager.setup($('#my video'), $('#my .original'), $('#my .modify'));
  myVideoProcess = myManager;
  $('.controller.video > .my-selector').append(myManager.createSelectTag());
  var otherManager = createManager();
  otherManager.setup($('#other video'), $('#other .original'), $('#other .modify'));
  $('.controller.video > .other-selector').append(otherManager.createSelectTag());

  $('.controller.video > .selector input').addClass('btn btn-success');
  $('.controller.video > .selector input[value="none"]').removeClass('btn-success');
  $('.controller.video > .selector input[value="none"]').addClass('btn-danger');

  $('.controller.video > .selector input').on('click',function(e){
    var me = $(this);
    if(!me.hasClass('btn-danger')){
      me.removeClass('btn-success btn-danger');
      me.addClass('btn-warning');
    }
    var prev = me.parent().find('.btn-warning').not(me);
    prev.removeClass('btn-warning');
    prev.addClass('btn-success');
  });
}

function setupAudioProcess(){
  $('.controller.audio .process-select').append(new LowPassNode(audioCtx, function(node){refreshNodes(node);}.bind(this)).createSelectTag());
  $('.controller.audio .process-select').append(new HighPassNode(audioCtx, function(node){refreshNodes(node);}.bind(this)).createSelectTag());
  $('.controller.audio .process-select').append(new PeakingNode(audioCtx, function(node){refreshNodes(node);}.bind(this)).createSelectTag());
  //and more...

  $('.controller.audio .process-select input').addClass('btn btn-success');
  $('.controller.audio .process-select input').on('click',function(e){
    var me = $(this);
    me.removeClass('btn-success btn-danger');
    me.addClass('btn-warning');
    var prev = me.parent().find('.btn-warning').not(me);
    prev.removeClass('btn-warning');
    prev.addClass('btn-success');
  });

  processedAnalyser = new SoundAnalyser(audioCtx);
  $('#my .process-analyser').append(processedAnalyser.createTag());

  destination = audioCtx.createMediaStreamDestination();
  processedAnalyser.getAnalyser().connect(destination);
}

//don't work.
function setupOtherAnalyser(stream){
  var analyser = new SoundAnalyser(audioCtx);
  $('#other .process-analyser').append(analyser.createTag());
  var sound = audioCtx.createMediaStreamSource(stream);
  sound.connect(analyser.getAnalyser());
  analyser.start();
}

//source incoming
function onGetSource(source){
  soundSource = source;
  refreshNodes();
}

//remove processes
function onPressRemoveProcess(){
  $('.controller.audio .process-select input').removeClass('btn-success btn-warning').addClass('btn-success');
  refreshNodes();
}

//restracture audio graph
function refreshNodes(node){
  $('.controller.audio .operations').empty();
  processedAnalyser.stop();
  soundSource.disconnect();
  if(currentNode){
    currentNode.disconnect();
    currentNode = undefined;
  }
  if(node){
    $('.controller.audio .operations').append(node.createOperationTag());
    node.setInput(soundSource);
    node.setOutput(processedAnalyser.getAnalyser());
    if(node.needMix() === true){
      soundSource.connect(processedAnalyser.getAnalyser());
    }
    currentNode = node;
  } else {
    soundSource.connect(processedAnalyser.getAnalyser());
  }
  processedAnalyser.start();
}
