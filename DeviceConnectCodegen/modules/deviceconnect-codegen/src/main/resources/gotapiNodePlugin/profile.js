'use strict';

let Profile = function(config) {
    this.name = config.name;
    this.operations = config.operations;
    this.eventList = [];
};

Profile.prototype._createPath = function(message) {
    if (message === undefined || message === null) {
        return null;
    }
    let request_url = message.request_url;
    if (request_url === undefined || request_url === null) {
        return null;
    }
    // URLクエリパラメータを削除
    request_url = request_url.split('?')[0];
    // プロファイルの最後に "/" が付いている場合は削除
    if (request_url.endsWith('/')) {
        request_url = request_url.substring(0, request_url.length - 1);
    }
    return request_url;
};

Profile.prototype._findOperation = function(path, method) {
    for (let key in this.operations) {
        let operation = this.operations[key];
        if (operation.method.toLowerCase() === method.toLowerCase()
            && operation.path.toLowerCase() === path.toLowerCase()) {
            return operation;
        }
    }
    return null;
};

Profile.prototype.receiveMessage = function(util, message) {
    let path = this._createPath(message);
    let operation = this._findOperation(path, message.method);
    if (operation !== null) {
        message['request_path'] = path;
        operation.onRequest(util, message);
    } else {
        message.result = 404;
    }
};

Profile.prototype.getEventListForPath = function(request) {
    let list = [];
    let requestPath = this._createPath(request);
    for (let k in this.eventList) {
        let event = this.eventList[k];
        let eventPath = this._createPath(event);
        if (eventPath.toLowerCase() === requestPath.toLowerCase()) {
            list.push(event);
        }
    }
    return list;
};

Profile.prototype.addEvent = function(message) {
    this.eventList.push(JSON.parse(JSON.stringify(message)));
};

Profile.prototype.removeEvent = function(message) {
    let requestPath = this._createPath(message);
    for (let k in this.eventList) {
        let event = this.eventList[k];
        let eventPath = this._createPath(event);
        if (eventPath.toLowerCase() === requestPath.toLowerCase()) {
            delete this.eventList[k];
            break;
        }
    }
};

module.exports = Profile;