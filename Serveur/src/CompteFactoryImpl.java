/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */


import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.sql.Connection;
import java.util.Enumeration;
import java.util.Hashtable;
import javafx.util.Pair;

/**
 *
 * @author Annabelle
 */
public class CompteFactoryImpl extends UnicastRemoteObject implements CompteFactory
{
    private ConnexionPool connexionPool;
    private Hashtable<Integer, Pair<CompteImpl, Integer>> comptes; //id_compte - <compte - nombre d'utilisateurs>
    private Hashtable<Connection, Integer> connexions; //connexion - nombre d'utilisations
    private final int SEUIL = 5;
    
    protected CompteFactoryImpl() throws RemoteException 
    {
        //Initiation
        super();
        this.connexionPool = new ConnexionPool();
        this.connexions = new Hashtable();
        this.comptes = new Hashtable();
    }
    
    private Connection getConnexion() throws RemoteException
    {
        Connection connexion = null;
        boolean connexion_ok = false;
        Enumeration e = this.connexions.keys();
        while(!connexion_ok && e.hasMoreElements()) 
        {
            connexion = (Connection)e.nextElement();
            int nb_utilisations = this.connexions.get(connexion);
            if(nb_utilisations<SEUIL)
            {
                //Si une connexion peut encore être utilisée
                connexion_ok = true;
                this.connexions.replace(connexion, nb_utilisations+1);
            }
        }
        if(!connexion_ok)
        {
            //S'il n'y a pas de connexion disponible
            connexion = this.connexionPool.getConnexion();
            this.connexions.put(connexion, 1);
        }
        this.affichageConnexions();
        return connexion;
    }
    
    private void returnConnexion(Connection connexion) throws RemoteException
    {
        int nb_utilisations = this.connexions.get(connexion);
        if(nb_utilisations<=1)
        {
            //La connexion n'est plus utilisée
            this.connexionPool.returnConnexion(connexion);
            this.connexions.remove(connexion);
        }
        else
        {
            //La connexion est encore utilisée
            this.connexions.replace(connexion, nb_utilisations-1);
        }
        this.affichageConnexions();
    }
    
    @Override
    public synchronized void libereCompte(Compte compte) throws RemoteException
    {
        int id = compte.getId();
        if(this.comptes.containsKey(id))
        {
            //Si le compte n'est plus utilisé, on l'enlève de la hashtable, sinon on décrémente le nombre d'utilisations
            int nb_utilisations = this.comptes.get(id).getValue();
            CompteImpl c = this.comptes.get(id).getKey();
            if(nb_utilisations>1)
            {
                this.comptes.replace(id, new Pair(c, nb_utilisations-1));
            }
            else
            {
                this.returnConnexion(c.getConnexion());
                this.comptes.remove(id);
            }
            this.affichageComptes();
        }
    }
    
    @Override
    public synchronized Compte getCompte(int id) throws RemoteException 
    {
        Compte c;
        //Si n'existe pas, on le crée, sinon on le récupère dans la hashtable en incrémentant le nombre d'utilisations
        if(!this.comptes.containsKey(id))
        {
            c = new CompteImpl(id, this.getConnexion());
            this.comptes.put(id, new Pair(c, 1));
        }
        else
        {
            c = this.comptes.get(id).getKey();
            int nb_utilisations = this.comptes.get(id).getValue();
            this.comptes.replace(id, new Pair(c, nb_utilisations+1));
        }
        this.affichageComptes();
        return c;
    }

    @Override
    public synchronized Compte createCompte(double d) throws RemoteException
    {
        Compte c = new CompteImpl(d, this.getConnexion());
        this.comptes.put(c.getId(), new Pair(c, 1));
        this.affichageComptes();
        return c;
    }
    
    private void affichageComptes() throws RemoteException
    {
        Enumeration e = this.comptes.elements(); 
        System.out.println("**********Liste des comptes et nombre d'utilisations**********"); 
        while(e.hasMoreElements()) 
        { 
            Pair p = (Pair)e.nextElement();
            System.out.println("Compte "+((Compte)p.getKey()).getId()+", nombre d'utilisations : "+p.getValue()); 
        }
        System.out.println("**************************************************************"); 
    }
    
    private void affichageConnexions() throws RemoteException
    {
        Enumeration e = this.connexions.keys(); 
        System.out.println("**********Liste des connexions et nombre d'utilisations**********"); 
        while(e.hasMoreElements()) 
        { 
            Connection c = (Connection)e.nextElement();
            System.out.println("Connexion "+c.toString()+", nombre d'utilisations : "+this.connexions.get(c)); 
        }
        System.out.println("*****************************************************************"); 
    }
}
