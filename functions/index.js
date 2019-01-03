const functions = require('firebase-functions');

const admin = require('firebase-admin');
admin.initializeApp(functions.config().firebase);
var tema = "Todos";
exports.notificaFoto = functions.firestore.document('eventos/{evento}')
    .onWrite(event => {
        if (event.after.data()['imagen'] != event.before.data()['imagen']) {
            const datos = {
                notification: {
                    title: 'Nueva imagen de ' + event.after.data()['evento'],
                    body: "Se ha cambiado la imagen del evento " + event.after.data()['evento']
                }
            };
            admin.messaging().sendToTopic(tema, datos).then(function (response) {
                console.log("Envio correcto:", response);
            }).catch(function (error) {
                console.log("Envío erróneo:", error);
            });
        }
    });