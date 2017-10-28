/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */


import java.rmi.ConnectException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;

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
            Compte c = fac.getCompte(1);
            System.out.println(c.getSolde());
            fac.libereCompte(c);
            /*try
            {
            Registry registry = LocateRegistry.getRegistry("localhost", 1099);
            CompteFactory fac = (CompteFactory)registry.lookup("CompteFactory");
            Compte c = fac.getCompte(1);
            Compte c1 = fac.getCompte(1);
            JOptionPane.showConfirmDialog(null, null, null, 1, 1, null);
            /*System.out.println(c1.getSolde());
            c.depot(10);
            System.out.println(c1.getSolde());
            c.retrait(10);
            System.out.println(c1.getSolde());
            List l = c.getArchivage();
            for(int i=0; i<l.size(); i++)
            System.out.println(l.get(i));*/
            /*fac.libereCompte(c);
            fac.libereCompte(c1);
            }
            catch (RemoteException | NotBoundException | SecurityException ex)
            {
            Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
            }*/
        } catch (ConnectException ex)
        {
            Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
        } catch (RemoteException ex)
        {
            Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
        }
    }   
}
