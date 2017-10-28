
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.Socket;
import java.rmi.MarshalledObject;
import java.rmi.Remote;
import java.util.StringTokenizer;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author Annabelle
 */
public class RMIServer extends Thread
{
    private Remote remote;
    private Class[] interfaces;
    private String nom;
    private boolean continuer;
    private String delimiteur;
    
    public RMIServer(Remote remote, String nom)
    {
        this.remote = remote;
        this.nom = nom;
        Class c = remote.getClass();
        this.interfaces = c.getInterfaces();
        this.continuer = true;
        this.delimiteur = "%";
    }

    public void arret()
    {
        this.continuer = false;
    }
    
    private String[] parseMsg(String msg)
    {
        //Format de la requête
        //<header><delim><port><delim><interface><delim><serviceName>
        StringTokenizer token = new StringTokenizer(msg, this.delimiteur);
        token.nextToken(); //header
        String[] strArray = new String[3];
        strArray[0] = token.nextToken(); //port de réponse
        strArray[1] = token.nextToken(); //nom de l'interface
        strArray[2] = token.nextToken(); //nom du service
        return strArray;            
    }
    
    @Override
    public void run()
    {
        try
        {
            MulticastSocket socket = new MulticastSocket(6789); //Port multicast
            InetAddress address = InetAddress.getByName("228.5.6.7"); //Adresse multicast
            socket.joinGroup(address);

            while(this.continuer)
            {
                byte[] buffer = new byte[512];
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                //Réception d'un message
                socket.receive(packet);
                String message = new String(packet.getData());
                System.out.println("RMIServer : Réception d'un message " + message);
                if(message.startsWith("RMI-Discovery"))
                {
                    //Lecture du contenu du message
                    String[] params = parseMsg(message);
                    //Récupération de l'adresse et du port du client
                    InetAddress adresse_retour = packet.getAddress();
                    int port_retour = Integer.parseInt(params[0]);
                    
                    String interface_demandee = params[1];
                    String service_demande = params[2].trim(); 
                    System.out.println("RMIServer : service=" + service_demande + ", adresse retour=" + adresse_retour + ", port retour=" + port_retour);
                    
                    //Vérification de l'existance de l'interface demandée
                    boolean match = false;
                    for(int i=0; !match && i<this.interfaces.length; i++)
                    {
                        match = this.interfaces[i].getName().equals(interface_demandee);
                    }
                    
                    //Vérification du service demandé
                    if(match && service_demande.equals(this.nom))
                    {
                        //Si l'interface existe, on envoie son stub
                        Socket sock = new Socket(adresse_retour, port_retour);
                        ObjectOutputStream oos = new ObjectOutputStream(sock.getOutputStream());
                        oos.writeObject(new MarshalledObject(this.remote));
                        oos.flush();
                        oos.close();
                    }
                    else
                    {
                        System.out.println("RMIServer : Pas d'interface correspondante");
                    }
                }
            }
            socket.leaveGroup(address);
            socket.close();         
        }
        catch(IOException ex)
        {
            ex.printStackTrace(System.err);
        }
    } 
}
