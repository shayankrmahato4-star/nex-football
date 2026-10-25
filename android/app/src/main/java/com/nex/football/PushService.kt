package com.nex.football

/*
 * Firebase Cloud Messaging (optional).
 *
 * Enable by:
 *   1. Adding app/google-services.json from your own Firebase project.
 *   2. Uncommenting the firebase dependencies in app/build.gradle.kts and the
 *      google-services plugin, plus the <service> entry in AndroidManifest.xml.
 *
 * Payloads must stay generic ("You have a new message") so no plaintext message
 * content ever travels through push - the client fetches and decrypts itself.
 *
 * import com.google.firebase.messaging.FirebaseMessagingService
 * import com.google.firebase.messaging.RemoteMessage
 *
 * class PushService : FirebaseMessagingService() {
 *     override fun onNewToken(token: String) { ...store token, web layer registers it... }
 *     override fun onMessageReceived(message: RemoteMessage) { ...show generic notification... }
 * }
 */
