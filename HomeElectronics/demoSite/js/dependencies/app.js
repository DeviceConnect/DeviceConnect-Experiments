/**
 app.js
 Copyright (c) 2015 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */

var app = (function (parent) {
  'use strict';

  var errorMessage = function(errorCode) {
    switch(errorCode) {
    case dConnect.constants.ErrorCode.ACCESS_FAILED:
      return 'Device Web APIサーバが見つからないか、接続が不安定です。<br>Device Connect Managerが起動しているかの確認や、約10~30秒ほど待ってからの操作をお試し下さい。';
    case dConnect.constants.ErrorCode.NOT_SUPPORT_PROFILE:
      return '現在サポートされていない機能です。<br>必要なプラグインがインストールされているかをご確認ください。';
    case dConnect.constants.ErrorCode.NOT_FOUND_SERVICE:
      return '指定されたデバイスを発見できませんでした。';
    case dConnect.constants.ErrorCode.TIMEOUT:
      return 'Device Web APIサーバとの通信がタイムアウトしました。';
    case dConnect.constants.ErrorCode.INVALID_ORIGIN:
      return 'Device Web APIへのアクセスが許可されませんでした。<br>ホワイトリストの設定をご確認ください。';
    case dConnect.constants.ErrorCode.ILLEGAL_DEVICE_STATE:
      return 'デバイス側の状態異常が発生しました。';
    case dConnect.constants.ErrorCode.ILLEGAL_SERVER_STATE:
      return 'プラグイン側の状態異常が発生しました。';
    case dConnect.constants.ErrorCode.NOT_SUPPORT_ACTION:
    case dConnect.constants.ErrorCode.NOT_SUPPORT_ATTRIBUTE:
      return 'このデバイスではサポートされていない機能です。';
    // 下記のエラーが発生した場合はアプリケーションの実装を確認する
    case dConnect.constants.ErrorCode.INVALID_SERVER:
    case dConnect.constants.ErrorCode.UNKNOWN:
    case dConnect.constants.ErrorCode.EMPTY_SERVICE_ID:
    case dConnect.constants.ErrorCode.UNKNOWN_ATTRIBUTE:
    case dConnect.constants.ErrorCode.INVALID_REQUEST_PARAMETER:
    case dConnect.constants.ErrorCode.AUTHORIZATION:
    case dConnect.constants.ErrorCode.EMPTY_ACCESS_TOKEN:
    case dConnect.constants.ErrorCode.SCOPE:
    case dConnect.constants.ErrorCode.NOT_FOUND_CLIENT_ID:
      return 'Device Web APIの実行に失敗しました。';
    default:
      return 'Device Web APIの実行に失敗しました。';
    }
  };
  parent.errorMessage = errorMessage;

  var isTemporaryError = function(errorCode) {
    switch(errorCode) {
    case dConnect.constants.ErrorCode.ACCESS_FAILED:
      return true;
    case dConnect.constants.ErrorCode.NOT_SUPPORT_PROFILE:
      return false;
    case dConnect.constants.ErrorCode.NOT_FOUND_SERVICE:
      return false;
    case dConnect.constants.ErrorCode.TIMEOUT:
      return true;
    case dConnect.constants.ErrorCode.INVALID_ORIGIN:
      return true;
    case dConnect.constants.ErrorCode.ILLEGAL_DEVICE_STATE:
      return true;
    case dConnect.constants.ErrorCode.ILLEGAL_SERVER_STATE:
      return false;
    case dConnect.constants.ErrorCode.NOT_SUPPORT_ATTRIBUTE:
      return true;
    case dConnect.constants.ErrorCode.NOT_SUPPORT_ACTION:
      return true;
    // 下記のエラーが発生した場合はアプリケーションの実装を確認する
    case dConnect.constants.ErrorCode.INVALID_SERVER:
    case dConnect.constants.ErrorCode.UNKNOWN:
    case dConnect.constants.ErrorCode.EMPTY_SERVICE_ID:
    case dConnect.constants.ErrorCode.UNKNOWN_ATTRIBUTE:
    case dConnect.constants.ErrorCode.INVALID_REQUEST_PARAMETER:
    case dConnect.constants.ErrorCode.AUTHORIZATION:
    case dConnect.constants.ErrorCode.EMPTY_ACCESS_TOKEN:
    case dConnect.constants.ErrorCode.SCOPE:
    case dConnect.constants.ErrorCode.NOT_FOUND_CLIENT_ID:
      return false;
    default:
      //知らないエラーは続行可能扱い
      return true;
    }
  };
  parent.isTemporaryError = isTemporaryError;

  return parent;
})({});
