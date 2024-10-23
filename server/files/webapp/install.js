/*
  Пример:
  <a href="javascript:void(0)" onclick="downloadInstaller()">Скачать дистрибутив клиента</a><span id="prc"></span>
  <a href="install">Альтернативная ссылка</a> (дождитесь начала загрузки, возможное время ожидания - несколько минут)

  Для использования профиля вместо downloadInstaller() используйте downloadInstallerFor("profile", "prc") и ссылку "install/profile"
 */

function downloadInstaller() {
    downloadInstallerFor("", "prc");
}

function downloadInstallerFor(prof, percentId) {
    var host = window.location.hostname;
    if (host) {
        var lhost = host.toLowerCase();
        if (lhost == 'localhost' || lhost == '127.0.0.1' || lhost == '::1') {
            if (!confirm('Вы используете адрес localhost для соединения с сервером. При этом вы не сможете подключиться к серверу с другого компьютера. Если вы хотите подключаться к серверу с других компьютеров, вместо localhost в адресе страницы введите сетевое имя сервера. Продолжить?'))
                return;
        }
    }
    var waiting = true;
    var profile = "";
    var isInit = 1;

    if (prof.length > 0) {
        profile = "/" + prof;
    }

    function onTimer() {
        if (waiting) {
            retrieveStatus(isInit);
            isInit = 0;
            setTimeout(onTimer, 1000);
        }
    }

    function getRequest() {
        var xmlhttp = false;
        /*@cc_on @*/
        /*@if (@_jscript_version >= 5)
         // JScript gives us Conditional compilation, we can cope with old IE versions.
         // and security blocked creation of the objects.
         try {
             xmlhttp = new ActiveXObject("Msxml2.XMLHTTP");
         } catch (e) {
             try {
                 xmlhttp = new ActiveXObject("Microsoft.XMLHTTP");
             } catch (E) {
                 xmlhttp = false;
             }
         }
         @end @*/
        if (!xmlhttp && typeof XMLHttpRequest != 'undefined') {
            try {
                xmlhttp = new XMLHttpRequest();
            } catch (e) {
                xmlhttp = false;
            }
        }
        if (!xmlhttp && window.createRequest) {
            try {
                xmlhttp = window.createRequest();
            } catch (e) {
                xmlhttp = false;
            }
        }
        return xmlhttp;
    }

    function retrieveStatus(isInit) {
        var xmlhttp = getRequest();
        xmlhttp.open("POST", "install" + profile + "?init=" + isInit, true);
        xmlhttp.onreadystatechange = function() {
            if (xmlhttp.readyState == 4) {
                var done = true;
                var percent = "";
                var ok = false;
                var error = "";
                if (xmlhttp.status == 200) {
                    var local = new Function("return " + xmlhttp.responseText)();
                    percent = local.percent;
                    done = local.done;
                    ok = local.ok;
                    error = local.error;
                }
                if (done) {
                    clearText();
                    if (waiting) {
                        waiting = false;
                        if (ok) {
                            var installUrl = "install" + profile;
                            if (!window.open(installUrl)) {
                                window.location.href = installUrl;
                            }
                        } else {
                            alert("Ошибка при создании установочного файла: " + error);
                        }
                    }
                } else {
                    setText(percent);
                }
            }
        };
        xmlhttp.send(null);
    }

    function clearText() {
        var elem = document.getElementById(percentId);
        while (elem.childNodes.length >= 1) {
            elem.removeChild(elem.firstChild);
        }
    }

    function setText(str) {
        clearText();
        var elem = document.getElementById(percentId)
        elem.appendChild(elem.ownerDocument.createTextNode(": " + str));
    }

    onTimer();
}
