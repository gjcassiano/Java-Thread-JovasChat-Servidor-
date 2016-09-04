package Servidor;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import javax.swing.text.DefaultCaret;
import static javax.swing.text.DefaultCaret.ALWAYS_UPDATE;

public class Servidor {

    private ServerSocket serversocket;
    private List<ConnectionToClient> ListaClientes;
    private FormServidor formservidor;
    private int qtd = 0;

    private class ConnectionToClient {

        ObjectOutputStream out;
        private ObjectInputStream in;
        Socket cliente;

        ConnectionToClient(Socket cliente, int qtd) throws IOException {
            this.cliente = cliente;
            out = new ObjectOutputStream(cliente.getOutputStream());
            out.flush();
            in = new ObjectInputStream(cliente.getInputStream());
            out.writeObject("Cliente Aceito!");
            out.flush();

            Thread read = new Thread() {
                public void run() {
                    try {
                        String message = "";
                        do {
                            try {
                                message = (String) in.readObject();
                                sendMessageAll("client[" + qtd + "]: " + message);
                                if (message.equals("bye")) {
                                    out.writeObject("bye");
                                }
                                out.flush();
                            } catch (Exception e) {
                            }
                        } while (!message.equals("bye"));
                        ListaClientes.remove(cliente);
                        printTextForm("Thread fechada do client[" + qtd + "]");
                    } catch (Exception e) {
                    }
                }
            };

            read.setDaemon(true); // terminate when main ends
            read.start();
        }

        public void write(Object obj) {
            try {
                out.writeObject(obj);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    void startServidor(FormServidor fs) {
        this.formservidor = fs;
        try {
            this.serversocket = new ServerSocket(8280);
            ListaClientes = new ArrayList<>();
            printTextForm("Servidor iniciado!");
        } catch (IOException ex) {
            printTextForm("Porta esta sendo usada");
        }
    }

    void printTextForm(String txt) {
        this.formservidor.jTextArea1.append(txt + "\n");

        DefaultCaret caret = (DefaultCaret) this.formservidor.jTextArea1.getCaret();
        caret.setUpdatePolicy(ALWAYS_UPDATE);
    }

    void accClient() {
        try {
            printTextForm("Esperando Cliente");
            Socket cliente = this.serversocket.accept();
            ConnectionToClient connectionToClient = new ConnectionToClient(cliente, qtd);
            ListaClientes.add(connectionToClient);
            // printTextForm("Cliente aceito!");
            printTextForm("Criando uma Thread para Cliente[" + qtd + "]!");
            qtd++;
        } catch (Exception e) {
        }

    }

    void sendMessageAll(String msg) {
        for (ConnectionToClient cliente : ListaClientes) {
            cliente.write(msg);
        }
        printTextForm(msg);

    }

}
