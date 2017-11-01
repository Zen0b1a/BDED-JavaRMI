/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */


import java.rmi.ConnectException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Annabelle
 */
public class Client
{

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args)
    {
        try
        {
            RMIClient rmi = new RMIClient(CompteFactory.class, "CompteFactory");          
            CompteFactory fac = rmi.recherche();
            List<Compte> liste = new ArrayList();
            //Récupération de plusieurs comptes
            for(int i=1; i<=20; i++)
            {
                liste.add(fac.getCompte(i));
                System.out.println("Solde du compte "+liste.get(i-1).getId()+" : "+liste.get((i-1)).getSolde());
            }
            //Test des méthodes de dépôt, de retrait et d'optention de l'archivage des opérations
            System.out.println(liste.get(5).getSolde());
            liste.get(5).depot(15);
            System.out.println(liste.get(5).getSolde());
            liste.get(5).retrait(25);
            System.out.println(liste.get(5).getSolde());
            List l = liste.get(5).getArchivage();
            for(int i=0; i<l.size(); i++)
                System.out.println(l.get(i));
            //Libération des comptes
            for(int i=20; i>0; i--)
            {
                fac.libereCompte(liste.get((i-1)));
                liste.remove((i-1));
            }
        } catch (ConnectException ex)
        {
            Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
        } catch (RemoteException ex)
        {
            Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
        }
    }   
}
