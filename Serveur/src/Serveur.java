/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */


/**
 *
 * @author ag092850
 */
public class Serveur {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        try
        {
            CompteFactory fac = new CompteFactoryImpl();
            RMIServer server = new RMIServer(fac, "CompteFactory");
            server.start();
            System.out.println("CompteFactory enregistré.");
        } 
        catch (Exception e) 
        {
            System.err.println("Erreur enregistrement CompteFactory : "+ e.getMessage());
        }
    }   
}
