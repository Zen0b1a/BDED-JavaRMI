/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */


import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 *
 * @author ag092850
 */
public interface CompteFactory extends Remote
{
    public Compte createCompte(double solde) throws RemoteException;
    public Compte getCompte(int id) throws RemoteException;
    public void libereCompte(Compte compte) throws RemoteException;
}
