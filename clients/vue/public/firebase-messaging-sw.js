// [START initialize_firebase_in_sw]
self.addEventListener("notificationclick", function(event) {
  console.log("background notification clicked", event);
  event.stopImmediatePropagation();
  event.notification.close();
  clients.openWindow(event.notification.data.FCM_MSG.data.url);
});

// Give the service worker access to Firebase Messaging.
// Note that you can only use Firebase Messaging here, other Firebase libraries
// are not available in the service worker.
importScripts("https://www.gstatic.com/firebasejs/7.19.1/firebase-app.js");
importScripts(
  "https://www.gstatic.com/firebasejs/7.19.1/firebase-messaging.js"
);
importScripts("./firebase.config.js");
// Initialize the Firebase app in the service worker by passing in the messagingSenderId.

firebase.initializeApp(firebaseConfig);

// Retrieve an instance of Firebase Messaging so that it can handle background messages.
const messaging = firebase.messaging();
messaging.onBackgroundMessage((payload) => {
  console.log("[firebase-messaging-sw.js] onBackgroundMessage", payload);
});
// [END initialize_firebase_in_sw]
