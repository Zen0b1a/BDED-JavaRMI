/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */


import java.io.Serializable;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

/**
 *
 * @author ag092850
 */
public interface Compte extends Remote, Serializable 
{ 
    public int getId() throws RemoteException;
    public double getSolde() throws RemoteException;
    public void retrait(double montant) throws RemoteException;
    public void depot(double montant) throws RemoteException;
    public List getArchivage() throws RemoteException;
}
