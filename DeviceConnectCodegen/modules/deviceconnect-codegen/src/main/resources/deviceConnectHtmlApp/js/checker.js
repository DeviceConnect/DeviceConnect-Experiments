
var main = (function(parent, global) {

    var _default_host = 'localhost';

    function init() {
        var host =  util.getCookie('dconnect-host') || _default_host;
        var hostElem = dom('host_address');
        hostElem.value = host;
        hostElem.addEventListener('change', function() {
            var newHost = hostElem.value;
            util.setCookie('dconnect-host', newHost);
            util.setHost(newHost);
            console.log('Cached host: ' + newHost);
        });
        util.setHost(host);
        
        parseJSON(specs);
    }
    parent.init = init;

    function findServices() {
        console.log('Start to find services.');
        var defaultName = dom('service_discovery').textContent;
        dom('service_discovery').disabled = true;
        dom('service_discovery').textContent = 'Finding...';
        util.serviceDiscovery(function(newServices) {
            var datalist = dom('cached_services');
            var service;
            var opt;
            // 古いサービス一覧を削除
            while (datalist.hasChildNodes()) {
                datalist.removeChild(datalist.lastChild);
            }
            // 新しいサービス一覧を作成
            for (var k in newServices) {
                service = newServices[k];
                opt = document.createElement('option');
                opt.label = service.name;
                opt.value = service.id;
                datalist.appendChild(opt);
            }

            dom('service_discovery').disabled = false;
            dom('service_discovery').textContent = defaultName;
            alert(newServices.length + '個のサービスが見つかりました。serviceIdの入力候補にて選択可能です。');
        }, function() {
            dom('service_discovery').disabled = false;
            showAlert("サービスの情報取得に失敗しました。", errorCode, errorMessage);
        });
    }
    parent.findServices = findServices;

    function parseJSON(json) {
        dom('main').innerHTML = createSupportPath(json.basePath, json.paths);
    }

    function dom(id) {
        return document.getElementById(id);
    }

    function hostAddress() {
        return dom('host_address').value;
    }

    function json(uri) {
        return xhr('GET', uri);
    }

    function xhr(method, uri, body) {
        return new Promise(function(resolve, reject) {
            var xhr = util.createXMLHttpRequest();
            xhr.onerror = function (e) {};
            xhr.onload = function (e) {};
            xhr.onreadystatechange = function() {
                switch (xhr.readyState) {
                case 1: {
                    xhr.send(body);
                } break;
                case 4: {
                    if (xhr.status == 200) {
                        var json = JSON.parse(xhr.responseText);
                    }
                    resolve(json);
                } break;
                default: {

                } break;
                }
            };
            xhr.open(method, uri);
        });
    }

    function back() {
            location.href = "./index.html?serviceId=" + util.getServiceId();
    }
    parent.back = back;
    function onChangeValue(nav, name) {
        var elem = document.forms[nav];
        elem['t_' + name].value = elem[name].value;
    }
    parent.onChangeValue = onChangeValue;

    function isHiddenParam(name) {
        return name == 'deviceconnect.method' || name == 'deviceconnect.type' || name == 'deviceconnect.path';
    }

    function isIncludedParam(name, formElem) {
        var checkbox = formElem['include-' + name];
        if (!checkbox) {
            return false
        }
        return checkbox.checked;
    }

    function switchParam(checkbox, name) {
        var inputElem = checkbox.form.elements[name];
        var tableElem = checkbox.form.children['table-' +name];
        inputElem.disabled = !checkbox.checked;
        tableElem.className = 'request ' + (checkbox.checked ? 'included' : 'excluded');
    }
    parent.switchParam = switchParam;

    function createBody(nav) {
        var data = [];

        data.push("accessToken=" + util.getAccessToken());

        var formElem = document.forms[nav];
        for (var key in formElem) {
            var elem = formElem[key];
            if (elem && elem.tagName) {
                if (!isIncludedParam(elem.name, formElem)) {
                    // パラメータ省略
                } else if (elem.tagName.toLowerCase() == 'input') {
                    if (isHiddenParam(elem.name)) {
                        // 隠しパラメータ
                    } else if (elem.type == 'checkbox') {
                        // チェックボックスは省略指定のために使用
                    } else if (elem.type == 'file') {
                        // どうするべきか検討
                    } else if (elem.name.indexOf('t_') != 0) {
                        if (elem.value.length != 0) {
                            data.push(elem.name + "=" + encodeURIComponent(elem.value));
                        }
                    }
                } else if (elem.tagName.toLowerCase() == 'select') {
                    data.push(elem.name + "=" + encodeURIComponent(elem.value));
                }
            }
        }

        return data;
    }

    function createFormData(nav) {
        var formData = new FormData();

        formData.append('accessToken', util.getAccessToken());

        var formElem = document.forms[nav];
        for (var key in formElem) {
            var elem = formElem[key];
            if (elem && elem.tagName) {
                if (!isIncludedParam(elem.name, formElem)) {
                    // パラメータ省略
                } else if (elem.tagName.toLowerCase() == 'input') {
                    if (isHiddenParam(elem.name)) {
                        // 隠しパラメータ
                    } else if (elem.type == 'checkbox') {
                        // チェックボックスは省略指定のために使用
                    } else if (elem.type == 'file') {
                        formData.append(elem.name, elem.files[0]);
                    } else if (elem.name.indexOf('t_') != 0) {
                        if (elem.value.length != 0) {
                            formData.append(elem.name, elem.value);
                        }
                    }
                } else if (elem.tagName.toLowerCase() == 'select') {
                    var option = elem.options[elem.selectedIndex];
                    if (option.dataset.excluded == 'true') {
                        // パラメータ省略
                    } else {
                        formData.append(elem.name, elem.value);
                    }
                }
            }
        }
        return formData;
    }

    function onSendRequest(nav) {
        var formElem = document.forms[nav];

        var method = formElem['deviceconnect.method'].value;
        var path = formElem['deviceconnect.path'].value;
        var xType = formElem['deviceconnect.type'].value;
        var body = null;

        hideResponseText(nav);
        hideEventText(nav);

        if (xType == 'event') {
            var uri = path.toLowerCase() + "?" + createBody(nav).join('&');

            setRequestText(nav, createRequest(method + " " + path));

            if (method == 'PUT') {
                dConnect.addEventListener(getUri(uri), function(json) {
                    setEventText(nav, createEvent(util.formatJSON(json)));
                }, function(json) {
                    setResponseText(nav, createResponse(util.formatJSON(JSON.stringify(json))));
                }, function(errorCode, errorMessage) {
                    setResponseText(nav, createResponse("errorCode=" + errorCode + " errorMessage=" + errorMessage));
                });
            } else {
                dConnect.removeEventListener(getUri(uri), function(json) {
                    setResponseText(nav, createResponse(util.formatJSON(JSON.stringify(json))));
                }, function(errorCode, errorMessage) {
                    setResponseText(nav, createResponse("errorCode=" + errorCode + " errorMessage=" + errorMessage));
                });
            }
        } else {
            if (method == 'GET' || method == 'DELETE') {
                path = path + "?" + createBody(nav).join('&');
            } else {
                body = createFormData(nav);
            }

            setRequestText(nav, createRequest(method + " " + path + "<br><br>" + body));

            util.sendRequest(method, util.getUri(path), body, function(status, response) {
                if (status == 200) {
                    setResponseText(nav, createResponse(util.formatJSON(response)));
                } else {
                    setResponseText(nav, createResponse("Http Status: " + status + "<br><br>" + response));
                }
            });
        }
    }
    parent.onSendRequest = onSendRequest;

    function getUri(path) {
        return 'http://' + hostAddress() + ':4035' + path; 
    }

    function setRequestText(nav, requestText) {
        document.getElementById(nav + '_request').innerHTML = requestText;
    }

    function setResponseText(nav, responseText) {
        document.getElementById(nav + '_response').innerHTML = responseText;
    }

    function setEventText(nav, eventText) {
        document.getElementById(nav + '_event').innerHTML = eventText;
    }

    function hideResponseText(nav) {
        document.getElementById(nav + '_response').innerHTML = "";
    }

    function hideEventText(nav) {
        document.getElementById(nav + '_event').innerHTML = "";
    }

    function createDConnectPath(basePath, path) {
        if (basePath !== undefined) {
            return basePath + path;
        }
        if (path === '/') {
            path = '';
        }
        return '/gotapi' + path;
    }

    function createTextParam(name, value, on) {
        var data = {
            'name' : name,
            'value' : value,
            'included' : (on ? 'included' : 'excluded'),
            'checkbox' : (on ? 'checked disabled' : ''),
            'inputable' : (on ? '' : 'disabled')
        };
        return util.createTemplate('param_text', data);
    }

    function createFileParam(name, on) {
        var data = {
            'name' : name,
            'included' : (on ? 'included' : 'excluded'),
            'checkbox' : (on ? 'checked disabled' : ''),
            'inputable' : (on ? '' : 'disabled')
        };
        return util.createTemplate('param_file', data);
    }

    function createNumberParam(name, value, on) {
        var data = {
            'name' : name,
            'value' : value,
            'included' : (on ? 'included' : 'excluded'),
            'checkbox' : (on ? 'checked disabled' : ''),
            'inputable' : (on ? '' : 'disabled')
        };
        return util.createTemplate('param_number', data);
    }

    function createTextServiceIdParam(name, value, on) {
        var data = {
            'name' : name,
            'value' : value,
            'included' : (on ? 'included' : 'excluded'),
            'checkbox' : (on ? 'checked disabled' : ''),
            'inputable' : (on ? '' : 'disabled')
        };
        return util.createTemplate('param_service_id', data);
    }

    function createSelectParam(name, list, on) {
        var text = "";
        for (var i = 0; i < list.length; i++) {
            text += '<option value="' + list[i] + '">' + list[i] + '</option>';
        }
        var data = {
            'name' : name,
            'value' : text,
            'included' : (on ? 'included' : 'excluded'),
            'checkbox' : (on ? 'checked disabled' : ''),
            'inputable' : (on ? '' : 'disabled')
        };
        return util.createTemplate('param_select', data);
    }

    function createSliderParam(nav, name, min, max, step, on) {
        var data = {
            'nav' : nav,
            'name' : name,
            'value' : (max + min) / 2.0,
            'step' : step,
            'min' : '' + min,
            'max' : '' + max,
            'included' : (on ? 'included' : 'excluded'),
            'checkbox' : (on ? 'checked disabled' : ''),
            'inputable' : (on ? '' : 'disabled')
        };
        return util.createTemplate('param_slider', data);
    }
    function createBooleanParam(name, value, on) {
        var data = {
            'name' : name,
            'included' : (on ? 'included' : 'excluded'),
            'checkbox' : (on ? 'checked disabled' : ''),
            'inputable' : (on ? '' : 'disabled')
        };
        return util.createTemplate('param_boolean', data);
    }
            

    function createRequest(body) {
        var data = {
            'body' : body
        };
        return util.createTemplate('request', data);
    }

    function createResponse(body) {
        var data = {
            'body' : body
        };
        return util.createTemplate('response', data);
    }

    function createEvent(body) {
        var data = {
            'body' : body
        };
        return util.createTemplate('event', data);
    }

    function createParams(nav, params) {
        var contentHtml = "";
        for (var i = 0; i < params.length; i++) {
            var param = params[i];
            var on = param.required;
            switch (param.type) {
            case 'string':
                if (('enum' in param)) {
                    contentHtml += createSelectParam(param.name, param.enum, on);
                } else {
                    if (param.name == 'serviceId') {
                        contentHtml += createTextServiceIdParam(param.name, [], on);
                    } else {
                        contentHtml += createTextParam(param.name, '', on);
                    }
                }
                break;
            case 'array':
                contentHtml += createTextParam(param.name, '', on);
                break;
            case 'integer':
                if (('enum' in param)) {
                    contentHtml += createSelectParam(param.name, param.enum, on);
                } else if (('minimum' in param) && ('maximum' in param)) {
                    contentHtml += createSliderParam(nav, param.name, param.minimum, param.maximum, 1, on);
                } else {
                    contentHtml += createNumberParam(param.name, 0, on);
                }
                break;
            case 'number':
                if (('enum' in param)) {
                    contentHtml += createSelectParam(param.name, param.enum, on);
                } else if (('minimum' in param) && ('maximum' in param)) {
                    contentHtml += createSliderParam(nav, param.name, param.minimum, param.maximum, 0.01, on);
                } else {
                    contentHtml += createNumberParam(param.name, 0, on);
                }
                break;
            case 'file':
                contentHtml += createFileParam(param.name, on);
                break;
            case 'boolean':
                contentHtml += createBooleanParam(param.name, '', on);
                break;
            default:
                console.log("Error: " + param.type);
                break;
            }
        }
        return contentHtml;
    }

    function createParameter(method, basePath, path, xType, params) {
        var nav = method + '_' + path;
        var data = {
            'nav' : nav,
            'method' : method.toUpperCase(),
            'path' : createDConnectPath(basePath, path),
            'xtype' : xType,
            'content' : createParams(nav, params)
        };
        return util.createTemplate('param', data);
    }

    function createCommand(method, basePath, path, param) {
        var data = {
            'title': method.toUpperCase() + ' ' + createDConnectPath(basePath, path),
            'nav' : method + '_' + path,
            'content' : createParameter(method, basePath, path, param['x-type'], param.parameters)
        };
        return util.createTemplate('command', data);
    }

    function createSupportMethod(basePath, path, data) {
        var contentHtml = "";
        for (var method in data) {
            contentHtml += createCommand(method, basePath, path, data[method]);
        }
        return contentHtml;
    }

    function createSupportPath(basePath, paths) {
        var contentHtml = "";
        for (var path in paths) {
            contentHtml += createSupportMethod(basePath, path, paths[path]);
        }
        return contentHtml;
    }

    return parent;
})(main || {}, this.self || global);


function onToggleIcon(obj, id) {
    var minus = obj.getElementsByClassName('minus')[0];
    var plus = obj.getElementsByClassName('plus')[0];
    var elem = document.getElementById(id);
    if (elem.checked) {
        minus.style.display = 'none';
        plus.style.display = 'inline';
    } else {
        minus.style.display = 'inline';
        plus.style.display = 'none';
    }
}
