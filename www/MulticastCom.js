var MulticastCom = function (){ 
};


// var MulticastCom = window.plugins.MulticastCom; Permite o acesso às funções`
MulticastCom.prototype.register = function(successCallback, errorCallback, options) {
    if (errorCallback == null) { errorCallback = function() {}}

    if (typeof errorCallback != "function")  {
        console.log("PushNotification.register failure: failure parameter not a function");
        return
    }

    if (typeof successCallback != "function") {
        console.log("PushNotification.register failure: success callback parameter must be a function");
        return
    }

	cordova.exec(successCallback, errorCallback, "MuslticastCom", "multicast", [options]);
};

// Call this to unregister for push notifications
MulticastCom.prototype.unregister = function(successCallback, errorCallback) {
    if (errorCallback == null) { errorCallback = function() {}}

    if (typeof errorCallback != "function")  {
        console.log("PushNotification.unregister failure: failure parameter not a function");
        return
    }

    if (typeof successCallback != "function") {
        console.log("PushNotification.unregister failure: success callback parameter must be a function");
        return
    }

     cordova.exec(successCallback, errorCallback, "MuslticastCom", "multicast", []);
};

//-------------------------------------------------------------------

if(!window.plugins) {
    window.plugins = {};
}
if (!window.plugins.MulticastCom) {
    window.plugins.MulticastCom = new MulticastCom();
}
