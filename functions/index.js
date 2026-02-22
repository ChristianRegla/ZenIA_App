const { onCall, HttpsError } = require("firebase-functions/v2/https");
const { setGlobalOptions } = require("firebase-functions/v2");
const admin = require("firebase-admin");

admin.initializeApp();

setGlobalOptions({
  maxInstances: 10
});

exports.deleteChatHistory = onCall(async (request) => {

    if (!request.auth) {
        throw new HttpsError("unauthenticated", "Usuario no autenticado");
    }

    const uid = request.auth.uid;

    const chatRef = admin.firestore()
    .collection("usuarios")
    .doc(uid)
    .collection("chatHistory");

    const snapshot = await chatRef.get();

    const batch = admin.firestore().batch();

    snapshot.docs.forEach((doc) => {
        batch.delete(doc.ref);
    });

    await batch.commit();
    
    return { success: true };
});