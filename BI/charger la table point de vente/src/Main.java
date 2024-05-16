import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class Main {
    public static void main(String[] args) {
        String csvFilePath = "C:\\Users\\the cast\\OneDrive\\Bureau\\POS_DATA\\POS_segmentation_POS_Profil.csv";
        String jdbcURL = "jdbc:postgresql://localhost:5432/djezzy";
        String username = "postgres";
        String password = "postgres";

        try (Connection connection = DriverManager.getConnection(jdbcURL, username, password);
             BufferedReader reader = new BufferedReader(new FileReader(csvFilePath))) {

            String line;
            reader.readLine(); // Skip header line
            PreparedStatement preparedStatement = connection.prepareStatement(
                    "INSERT INTO dim_point_de_vente (code_pdv) " +
                            "SELECT ? " +
                            "WHERE NOT EXISTS (SELECT 1 FROM dim_point_de_vente WHERE code_pdv = ?)");

            while ((line = reader.readLine()) != null) {
                String[] parts = line.split("\t"); // Séparer les valeurs par tabulation
                String code_pdv = parts[0].trim().split("-")[0]; // Extraire uniquement l'identifiant de point de vente
                preparedStatement.setString(1, code_pdv);
                preparedStatement.setString(2, code_pdv);
                preparedStatement.addBatch();
            }

            preparedStatement.executeBatch();
            System.out.println("Données insérées avec succès  .");

        } catch (SQLException e) {
            System.out.println("Erreur lors de la connexion à la base de données : " + e.getMessage());
        } catch (IOException e) {
            System.out.println("Erreur lors de la lecture du fichier CSV : " + e.getMessage());
        } catch (Exception e) {
            System.out.println("Erreur : " + e.getMessage());
        }
    }
}
