importScripts(
  "https://cdnjs.cloudflare.com/ajax/libs/localforage/1.9.0/localforage.min.js"
);

function generateDeviceId() {
  const chars =
    "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
  let deviceId = "";
  for (let i = 0; i < 200; i++) {
    deviceId += chars[Math.floor(Math.random() * chars.length)];
  }
  return deviceId;
}

function getDeviceId() {
  const tokenKey = "deviceId";

  return localforage.getItem(tokenKey).then(function(deviceId) {
    if (!deviceId) {
      deviceId = generateDeviceId();
      return localforage.setItem(tokenKey, deviceId).then(function() {
        return deviceId;
      });
    }
    return deviceId;
  });
}

workbox.core.setCacheNameDetails({ prefix: "vue" });

self.addEventListener("message", (event) => {
  if (event.data && event.data.type === "SKIP_WAITING") {
    self.skipWaiting();
  }

  if (event.data.type == "getDeviceId") {
    getDeviceId().then(function(deviceId) {
      event.source.postMessage({ type: "putDeviceId", data: deviceId });
    });
  }

  if (event.data.type == "setApiBasePath") {
    localforage.setItem("apiBasePath", event.data.data);
  }
});

self.__precacheManifest = [].concat(self.__precacheManifest || []);
workbox.precaching.precacheAndRoute(self.__precacheManifest, {});

// ################################
// notifications code from here on!
function showNotification(notification) {
  // we could do the permission check before sending the request, but this could result in following scenario:
  //  * user enables notifications
  //  * user disables notifications
  //  * user enables notifications after some weeks -> now the server thinks that there are a lot of open notifications.
  // but since we poll them regularly this cannot happen.
  // Additionally we could send also other stuff with this request to the server in the furture.
  if (Notification.permission == "granted") {
    self.registration.showNotification(notification.message);
  }
}

function showNotificationIfNeeded() {
  getDeviceId()
    .then(function(deviceId) {
      return localforage.getItem("apiBasePath").then(function(base) {
        const url = base + "/user/notifications?deviceId=" + deviceId;
        return fetch(url)
          .then((response) => response.json())
          .then((notifications) =>
            notifications.forEach((notification) =>
              showNotification(notification)
            )
          );
      });
    })
    .catch((error) => console.error(error));
}

// check for notifications every minute
setInterval(showNotificationIfNeeded, 1000 * 60 * 1);
// ################################
