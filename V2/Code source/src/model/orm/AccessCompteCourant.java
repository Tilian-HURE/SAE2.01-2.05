package model.orm;


import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import model.data.CompteCourant;
import model.orm.exception.DataAccessException;
import model.orm.exception.DatabaseConnexionException;
import model.orm.exception.ManagementRuleViolation;
import model.orm.exception.Order;
import model.orm.exception.RowNotFoundOrTooManyRowsException;
import model.orm.exception.Table;


public class AccessCompteCourant {

	
	public AccessCompteCourant() {}

	
	/**
	 * Recherche des CompteCourant d'un client à partir de son id.
	 *
	 * @param idNumCli id du client dont on cherche les comptes
	 * @return Tous les CompteCourant de idNumCli (ou liste vide)
	 * @throws DataAccessException
	 * @throws DatabaseConnexionException
	 */
	public ArrayList<CompteCourant> getCompteCourants(int idNumCli)
			throws DataAccessException, DatabaseConnexionException {
		ArrayList<CompteCourant> alResult = new ArrayList<>();

		try {
			Connection con = LogToDatabase.getConnexion();
			String query = "SELECT * FROM CompteCourant where idNumCli = ?";
			query += " ORDER BY idNumCompte";

			PreparedStatement pst = con.prepareStatement(query);
			pst.setInt(1, idNumCli);
			System.err.println(query);

			ResultSet rs = pst.executeQuery();
			while (rs.next()) {
				int idNumCompte = rs.getInt("idNumCompte");
				int debitAutorise = rs.getInt("debitAutorise");
				double solde = rs.getDouble("solde");
				String estCloture = rs.getString("estCloture");
				int idNumCliTROUVE = rs.getInt("idNumCli");

				alResult.add(new CompteCourant(idNumCompte, debitAutorise, solde, estCloture, idNumCliTROUVE));
			}
			rs.close();
			pst.close();
		} catch (SQLException e) {
			throw new DataAccessException(Table.CompteCourant, Order.SELECT, "Erreur accès", e);
		}

		return alResult;
	}
	
	
	/**
	 * Enregistre un compte avec ses différentes valeurs en paramètres.
	 * @param compte : compte à enregistrer
	 * @throws DataAccessException
	 * @throws DatabaseConnexionException
	 * @throws RowNotFoundOrTooManyRowsException 
	 */
	public void enregistrerCompte(CompteCourant compte)
			throws DataAccessException, DatabaseConnexionException, RowNotFoundOrTooManyRowsException {

		try {
			Connection con = LogToDatabase.getConnexion();
			
			String query = "INSERT INTO COMPTECOURANT VALUES (" + "seq_id_client.NEXTVAL" + " ," + "?" + ", " + "?" + 
			", " + "?" + ", " + "?" + ")";
			
			PreparedStatement pst = con.prepareStatement(query);
			if(compte.debitAutorise > 0){
				pst.setInt(1, - compte.debitAutorise) ;
			}
			else pst.setInt(1, compte.debitAutorise) ;
			pst.setDouble(2, compte.solde) ; 
			pst.setInt(3, compte.idNumCli) ; 
			pst.setString(4, compte.estCloture) ; 
			
			int result = pst.executeUpdate();
			
			System.err.println(result);
			if (result != 1) {
			        con.rollback();
			        throw new RowNotFoundOrTooManyRowsException(Table.Client, Order.INSERT,
			                        "Insert anormal (insert de moins ou plus d'une ligne)", null, result);
			} else {
			    con.commit();
			}
			
			System.err.println(query);
			pst.close();
			
		} catch (SQLException e) {
			throw new DataAccessException(Table.CompteCourant, Order.SELECT, "Erreur accès", e);
		}
	}
	
	
	/**
	 * Clôture un compte dans la base de données.
	 * @param pfNumCompte : le numéro du compte à clôturer
	 * @throws DataAccessException
	 * @throws DatabaseConnexionException
	 * @throws RowNotFoundOrTooManyRowsException
	 */
	public void cloturerCompte(int pfNumCompte)
			throws DataAccessException, DatabaseConnexionException, RowNotFoundOrTooManyRowsException {

		try {
			Connection con = LogToDatabase.getConnexion();
			
			String query = "UPDATE COMPTECOURANT "+ "SET ESTCLOTURE = 'O', SOLDE = ?" +
			"WHERE IDNUMCOMPTE = ?" ; 
			
			PreparedStatement pst = con.prepareStatement(query);
			
			pst.setInt(2, pfNumCompte);
			pst.setInt(1, 0);
			int result = pst.executeUpdate();
			
			System.err.println(result);
			if (result != 1) {
			        con.rollback();
			        throw new RowNotFoundOrTooManyRowsException(Table.Client, Order.INSERT,
			                        "Insert anormal (insert de moins ou plus d'une ligne)", null, result);
			} else {
			    con.commit();
			}
			
			System.err.println(query);
			pst.close();
			
		} catch (SQLException e) {
			throw new DataAccessException(Table.CompteCourant, Order.SELECT, "Erreur accès", e);
		}

	}
	
	
	/**
	 * Recherche d'un CompteCourant à partir de son id (idNumCompte).
	 * @param idNumCompte id du compte (clé primaire)
	 * @return Le compte ou null si non trouvé
	 * @throws RowNotFoundOrTooManyRowsException
	 * @throws DataAccessException
	 * @throws DatabaseConnexionException
	 */
	public CompteCourant getCompteCourant(int idNumCompte)
			throws RowNotFoundOrTooManyRowsException, DataAccessException, DatabaseConnexionException {
		try {
			CompteCourant cc;

			Connection con = LogToDatabase.getConnexion();

			String query = "SELECT * FROM CompteCourant where" + " idNumCompte = ?";

			PreparedStatement pst = con.prepareStatement(query);
			pst.setInt(1, idNumCompte);

			System.err.println(query);

			ResultSet rs = pst.executeQuery();

			if (rs.next()) {
				int idNumCompteTROUVE = rs.getInt("idNumCompte");
				int debitAutorise = rs.getInt("debitAutorise");
				double solde = rs.getDouble("solde");
				String estCloture = rs.getString("estCloture");
				int idNumCliTROUVE = rs.getInt("idNumCli");

				cc = new CompteCourant(idNumCompteTROUVE, debitAutorise, solde, estCloture, idNumCliTROUVE);
			} else {
				rs.close();
				pst.close();
				return null;
			}

			if (rs.next()) {
				throw new RowNotFoundOrTooManyRowsException(Table.CompteCourant, Order.SELECT,
						"Recherche anormale (en trouve au moins 2)", null, 2);
			}
			rs.close();
			pst.close();
			return cc;
		} catch (SQLException e) {
			throw new DataAccessException(Table.CompteCourant, Order.SELECT, "Erreur accès", e);
		}
	}

	
	/**
	 * Mise à jour d'un CompteCourant.
	 *
	 * cc.idNumCompte (clé primaire) doit exister seul cc.debitAutorise est mis à
	 * jour cc.solde non mis à jour (ne peut se faire que par une opération)
	 * cc.idNumCli non mis à jour (un cc ne change pas de client)
	 *
	 * @param cc IN cc.idNumCompte (clé primaire) doit exister seul
	 * @throws RowNotFoundOrTooManyRowsException
	 * @throws DataAccessException
	 * @throws DatabaseConnexionException
	 * @throws ManagementRuleViolation
	 */
	public void updateCompteCourant(CompteCourant cc) throws RowNotFoundOrTooManyRowsException, DataAccessException,
			DatabaseConnexionException, ManagementRuleViolation {
		try {

			CompteCourant cAvant = this.getCompteCourant(cc.idNumCompte);
			if (cc.debitAutorise > 0) {
				cc.debitAutorise = -cc.debitAutorise;
			}
			if (cAvant.solde < cc.debitAutorise) {
				throw new ManagementRuleViolation(Table.CompteCourant, Order.UPDATE,
						"Erreur de règle de gestion : sole à découvert", null);
			}
			Connection con = LogToDatabase.getConnexion();

			String query = "UPDATE CompteCourant SET " + "debitAutorise = ? " + "WHERE idNumCompte = ?";

			PreparedStatement pst = con.prepareStatement(query);
			pst.setInt(1, cc.debitAutorise);
			pst.setInt(2, cc.idNumCompte);

			System.err.println(query);

			int result = pst.executeUpdate();
			pst.close();
			if (result != 1) {
				con.rollback();
				throw new RowNotFoundOrTooManyRowsException(Table.CompteCourant, Order.UPDATE,
						"Update anormal (update de moins ou plus d'une ligne)", null, result);
			}
			con.commit();
		} catch (SQLException e) {
			throw new DataAccessException(Table.CompteCourant, Order.UPDATE, "Erreur accès", e);
		}
	}
	
}
