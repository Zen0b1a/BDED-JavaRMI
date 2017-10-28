/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */


import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import javafx.util.Pair;

/**
 *
 * @author Annabelle
 */
public class CompteFactoryImpl extends UnicastRemoteObject implements CompteFactory
{
    private ConnexionPool connexionPool;
    private Hashtable<Integer, Pair<CompteImpl, Integer>> comptes; //id_compte - <compte - nombre d'utilisateurs>
    private List<Pair<Connection, Integer>> connexions; //connexion - nombre d'utilisations
    private final int SEUIL = 5;
    
    protected CompteFactoryImpl() throws RemoteException 
    {
        //Initiation
        super();
        this.connexionPool = new ConnexionPool();
        this.connexions = new ArrayList();
        this.comptes = new Hashtable();
    }
    
    private Connection getConnexion()
    {
        Connection connexion = null;
        for(int i=0; i<this.connexions.size(); i++)
        {
            if(this.connexions.get(i).getValue()<SEUIL)
            {
                //Si une connexion peut encore être utilisée
                connexion = this.connexions.get(i).getKey();
                this.connexions.set(i, new Pair(connexion, this.connexions.get(i).getValue()+1));
                i = this.connexions.size();
                System.out.println("Utilisation d'une connexion existante.");
            }
        }
        if(connexion==null)
        {
            //S'il n'y a pas de connexion disponible
            connexion = this.connexionPool.getConnexion();
            this.connexions.add(new Pair(connexion, 1));
            System.out.println("Utilisation d'une nouvelle connexion.");
        }
        return connexion;
    }
    
    private void returnConnexion(Connection connexion)
    {
        int nb_utilisations = 0;
        for(int i=0; i<this.connexions.size(); i++)
        {
            if(this.connexions.get(i).getKey().equals(connexion))
            {
                nb_utilisations = this.connexions.get(i).getValue();
                if(nb_utilisations<=1)
                {
                    //La connexion n'est plus utilisée
                    this.connexionPool.returnConnexion(this.connexions.get(i).getKey());
                    this.connexions.remove(i);
                    System.out.println("Retrait d'une connexion.");
                }
                else
                {
                    //La connexion est encore utilisée
                    this.connexions.set(i, new Pair(this.connexions.get(i).getKey(), nb_utilisations-1));
                    System.out.println("Décrémentation de l'utilisation de la connexion.");
                }
                i = this.connexions.size();
            }
        }
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
                System.out.println("Nombre d'utilisations du compte "+id+" : "+(nb_utilisations-1));
            }
            else
            {
                this.returnConnexion(c.getConnexion());
                this.comptes.remove(id);
                System.out.println("Retrait du compte "+id);
            }
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
            System.out.println("Ajout du compte "+id);
        }
        else
        {
            c = this.comptes.get(id).getKey();
            int nb_utilisations = this.comptes.get(id).getValue();
            this.comptes.replace(id, new Pair(c, nb_utilisations+1));
            System.out.println("Nombre d'utilisations du compte "+id+" : "+(nb_utilisations+1));
        }
        return c;
    }

    @Override
    public synchronized Compte createCompte(double d) throws RemoteException
    {
        Compte c = new CompteImpl(d, this.getConnexion());
        this.comptes.put(c.getId(), new Pair(c, 1));
        return c;
    }
}
