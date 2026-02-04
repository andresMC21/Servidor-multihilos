import java.io.*;
import java.net.*;
import java.util.*;

public class ServidorMultihilos {

    // Debido a diversas excepciones presentes durante la apertura del servidor,
    // nuestro método main
    // debe arrojar excepciones.
    public static void main(String[] args) throws Exception {

        try {

            // Se crea el socket 'listenerSocket' para atender servicios en el puerto 6789
            ServerSocket listenerSocket = new ServerSocket(6789);

            // Este bucle permitira que se cree un nuevo hilo cada vez que el socket
            // listener atienda una solicitud en el servidor por el puerto especificado.
            while (true) {

                // Se crea el socket de conexión 'connectionSocket' mediante el uso del método
                // .accept()
                Socket connectionSocket = listenerSocket.accept();
                Thread newThread = new Thread(new ThreadController(connectionSocket));
                newThread.start();

            }

        } catch (IOException e) {

            System.out.println("Starting server error");

        }

    }

    static class ThreadController implements Runnable { // Clase auxiliar para el control de hilos 

        private Socket connectionSocket;

        public ThreadController(Socket connectionSocket) {
            this.connectionSocket = connectionSocket;
        }

        @Override
        public void run() {

            System.out.println("New thread started: " + Thread.currentThread().getName());

            try {

                String requestLineHttp;
                String fileName;

                // Proseguimos a crear dos emisores: clientMessage y messageForClient

                BufferedReader clientMessage = new BufferedReader(
                        new InputStreamReader(connectionSocket.getInputStream()));
                DataOutputStream messageForClient = new DataOutputStream(
                        new DataOutputStream(connectionSocket.getOutputStream()));

                // Ahora, se lee la primera linea del mensaje HTTP
                // se supone que esta linea tiene la forma:
                // GET nombre_archivo HTTP/1.0

                requestLineHttp = clientMessage.readLine();

                // Tras obtener la linea solicitada, esta puede imaginarse como la linea de la
                // solicitud HTTP
                // separa en tres partes: "GET", "nombre_archivo" y "HTTP/1.0"

                StringTokenizer splitLine = new StringTokenizer(requestLineHttp);

                // Se realiza la primera validación de la linea esperada: GET

                if (splitLine.nextToken().equals("GET")) {

                    fileName = splitLine.nextToken(); // Se obtiene el nombre del archivo

                    // Se valida si el nombre obtenido contiene el prefix "/", si es el caso se
                    // remueve
                    if (fileName.startsWith("/") == true) {

                        fileName = fileName.substring(1);

                    }

                    // Se crea el objeto del archivo

                    File file = new File(fileName);

                    System.out.println("Requested file: " + fileName);

                    // La siguiente línea asocia un emisor, 'inputFile', al archivo
                    // fileName

                    FileInputStream inputFile = new FileInputStream(fileName);

                    int bytes = (int) file.length();
                    byte[] fileBytes = new byte[bytes];

                    // Procedemos a leer la información del archivo de entrada y la almacenamos en
                    // el arreglo de bytes con el mismo tamaño

                    inputFile.read(fileBytes);

                    // Ahora construimos el mensaje de respuesta que el servidor enviara al cliente
                    // (browser)
                    // empleando la linea de respuesta en el emisor messageForClient

                    messageForClient.writeBytes("HTTP/1.0 200 Document Follows\r\n");
                    ;

                    // Se construye el encabezado del mensaje HTTP

                    if (fileName.endsWith(".jpg")) {

                        messageForClient.writeBytes("Content-Type: image/jpeg\r\n");

                    }

                    // Construye luego el encabezado para indicar la longitud del archivo
                    messageForClient.writeBytes("Content-Length: " + bytes + "\r\n");

                    // Ahora envia la linea en blanco que estipula el RFC de HTTP/1.0
                    messageForClient.writeBytes("\r\n");

                    // Finalmente envía el archivo solicitado al cliente
                    messageForClient.write(fileBytes, 0, bytes);

                    // Después de enviar el archivo, el servidor cierra el socket de conexión y el
                    // archivo enviado
                    inputFile.close();
                    connectionSocket.close();

                } else {

                    System.out.println("Incorrect request message");

                }

            } catch (IOException e) {

                System.out.println("Client control Failed");

            }
        }
    }
}
