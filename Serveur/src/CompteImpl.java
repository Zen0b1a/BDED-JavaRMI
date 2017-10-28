/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */


import java.rmi.MarshalledObject;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import oracle.jdbc.OraclePreparedStatement;
import oracle.jdbc.OracleResultSet;

/**
 *
 * @author Annabelle
 */
public class CompteImpl extends UnicastRemoteObject implements Compte
{
    private int id;
    private double solde;
    private Connection connexion;
    private MarshalledObject marshalledObject;
    
    public CompteImpl(double solde, Connection connexion) throws RemoteException
    {
        //Création d'un nouveau compte
        try 
        {
            this.connexion = connexion;
            CallableStatement cstmt = this.connexion.prepareCall("{?=call create_compte(?)}");
            cstmt.registerOutParameter(1, Types.INTEGER);
            cstmt.setDouble(2, solde);
            cstmt.execute();
            this.id = cstmt.getInt(1);
            this.solde = solde;
            cstmt.close();
        } 
        catch (SQLException ex) 
        {
            Logger.getLogger(CompteImpl.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public CompteImpl(int id, Connection connexion) throws RemoteException
    {
        //Récupération d'un compte existant
        this.connexion = connexion;
        this.id = id;
        try 
        {
            //Récupération du compte avec l'id
            OraclePreparedStatement stmt = (OraclePreparedStatement)this.connexion.prepareStatement("SELECT * FROM compte WHERE id=?");
            stmt.setInt(1, this.id);
            OracleResultSet rs = (OracleResultSet)stmt.executeQuery();
            if(rs.next())
            {
                //Récupération des informations du compte
                this.solde = rs.getDouble("SOLDE");
            }
            else
            {
                System.out.println("Le compte n'existe pas.");
                this.id = -1;
                this.solde = 0;
            }
            rs.close();
            stmt.close();
        } 
        catch (SQLException ex) 
        {
            Logger.getLogger(Compte.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    @Override
    public int getId()
    {
        return this.id;
    }
    
    @Override
    public double getSolde()
    {
        return this.solde;
    }
    
    public Connection getConnexion()
    {
        return this.connexion;
    }
    
    @Override
    public synchronized void retrait(double montant)
    {
        if(this.id != -1)
        {
            try 
            {
                //Retrait
                CallableStatement cstmt = this.connexion.prepareCall("{call retrait(?,?)}");
                cstmt.setInt(1, this.id);
                cstmt.setDouble(2, montant);
                cstmt.execute();
                cstmt.close();

                //Mise à jour du solde
                this.solde -= montant;

                System.out.println("Retrait de "+montant+"€");
            } 
            catch (SQLException ex) 
            {
                Logger.getLogger(Compte.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        else
            System.out.println("Retrait impossible : ce compte n'existe pas.");
    }
    
    @Override
    public synchronized void depot(double montant)
    {
        if(this.id != -1)
        {
            try 
            {
                //Depot
                CallableStatement cstmt = this.connexion.prepareCall("{call depot(?,?)}");
                cstmt.setInt(1, this.id);
                cstmt.setDouble(2, montant);
                cstmt.execute();
                cstmt.close();

                //Mise à jour du solde
                this.solde += montant;

                System.out.println("Dépôt de "+montant+"€");
            } 
            catch (SQLException ex) 
            {
                Logger.getLogger(Compte.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        else
            System.out.println("Dépôt impossible : ce compte n'existe pas.");
    }

    @Override
    public List getArchivage() throws RemoteException 
    {
        List archivage = new ArrayList();
        try 
        {
            //Récupération de l'archivage des opérations
            PreparedStatement stmt = (PreparedStatement)this.connexion.prepareStatement("SELECT * FROM operation_compte WHERE id_compte=?");
            stmt.setInt(1, this.id);
            ResultSet rs = (ResultSet)stmt.executeQuery();
            while(rs.next())
            {
                archivage.add("Date : "+rs.getDate("DATE_OPERATION")+", type : "+
                        rs.getString("TYPE_OPERATION")+", montant : "+rs.getDouble("MONTANT_OPERATION")+"€");
            }
            rs.close();
            stmt.close();
        } 
        catch (SQLException ex) 
        {
            Logger.getLogger(Compte.class.getName()).log(Level.SEVERE, null, ex);
        }
        return archivage;
    }
}
