
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.ServerSocket;
import java.net.Socket;
import java.rmi.MarshalledObject;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author Annabelle
 */
public class RMIClient
{
    private Class recherche; //Classe recherchée
    private String service; //Service recherché
    private ServerSocket listener; 
    private int portListener;
    private Object resultat; //Stub réponse du serveur
    private final Object lock = new Object(); //Verrou
    
    public RMIClient(Class recherche, String service)
    {
        this.recherche = recherche;
        this.service = service;
        this.portListener = 1026;
        this.listener = null;
    }
    
    private void startListener(int port, int range)
    {
        //On cherche un port disponible pour ouvrir le listener
        for(int i=port; this.listener==null && i<port+range; i++)
        {
            try
            {
                this.listener = new ServerSocket(i);
                this.portListener = i;
            }
            catch(IOException ex)
            {
                System.err.println("Port "+i+" exception "+ex.getMessage());
            }
        }
        if(this.listener==null)
        {
           throw new RuntimeException("Echec de la création du listener "+port+"-"+(port+range));
        }
        Thread listenerThread=new Thread()
        {
           public void run()
           {
                try
                {
                    //Récupération du stub
                    Socket socket = listener.accept();
                    ObjectInputStream ois = new ObjectInputStream(socket.getInputStream());
                    MarshalledObject mo = (MarshalledObject)ois.readObject();
                    socket.close();
                    resultat = mo.get();
                }
                catch(IOException | ClassNotFoundException ex)
                {
                    resultat = ex;
                }
                synchronized(lock)
                {
                    //Libération du verrou
                   lock.notify();
                }
           }
        };
        listenerThread.start();
        System.out.println("RMIClient : Démarrage du thread listener");
    }
    
    private void startRequester()
    {
        Thread requester=new Thread()
        {
            public void run()
            {
                try
                {   
                    InetAddress address = InetAddress.getByName("228.5.6.7"); //Adresse multicast, identique à celle du serveur
                    int multicastPort = 6789; //Port multicast, identique à celui du serveur
                    //Définition du header et du délimiteur
                    String header = "RMI-Discovery";
                    String delim = "%";
                    //Création du message à envoyer au serveur
                    String requete = header + delim + portListener + delim + recherche.getName() + delim + service;
                    byte [] buf = requete.getBytes();        
                            	
                    MulticastSocket socket = new MulticastSocket(multicastPort);
                    socket.joinGroup(address);
                    
                    int nb_essais = 7; //Nombre de tentatives d'envoi du message
                    DatagramPacket packet = new DatagramPacket(buf, buf.length, address,multicastPort);
                    for(int nTimes=0; resultat==null && nTimes<nb_essais;nTimes++)
                    {    
                        System.out.println("RMIClient : Envoi de la requête : "+requete);
                        socket.send(packet); //Envoi du datagram
                        Thread.sleep(5000);
                    }        
                    socket.leaveGroup(address);
                    socket.close();
                    if(resultat==null)
                    {
                        throw new Exception("RMIClient n'a pas trouvé le serveur");
                    }
                }
                catch(Exception ex)
                {
                    resultat=ex;
                    synchronized(lock)
                    {
                        //Libération du verrou
                        lock.notify();
                    }
                }
            }
        };
        requester.start();
        System.out.println("RMIClient : Démarrage du thread de requête");
    }
    
    public CompteFactory recherche() throws java.rmi.ConnectException
    {
        //Démarrage des threads
        startListener(this.portListener, 10);
        startRequester();
        //Attente de la libération du verrou
        synchronized(this.lock)
        {
            if(this.resultat==null)
            {
                try
                {
                    //Blocage du verrou
                    this.lock.wait();
                }
                catch(InterruptedException ex)
                {
                    ex.printStackTrace();
                    return null;
                }
            }
        }
        try
        {
            this.listener.close();
        }
        catch(IOException ex)
        {
            ex.printStackTrace(System.err);
        }
        
        //Vérification que le résultat obtenu n'est pas une exception
        if(this.resultat instanceof Exception)
        {
            throw new java.rmi.ConnectException("RMIClient exception",(Exception)this.resultat);
        }
        return (CompteFactory)this.resultat;
    }
}
