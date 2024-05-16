import java.io.BufferedReader;
import java.io.FileReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class Main {
    // Informations de connexion à la base de données
    static final String URL = "jdbc:postgresql://localhost:5432/djezzy";
    static final String USER = "postgres";
    static final String PASSWORD = "postgres";
    public static void loadUserData(String csvFilePath) {
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement pstmt = conn.prepareStatement("INSERT INTO dim_transaction (brand_name) " +
                     "SELECT DISTINCT ? WHERE NOT EXISTS (SELECT 1 FROM dim_transaction WHERE brand_name = ?)")) {

            BufferedReader br = new BufferedReader(new FileReader(csvFilePath));
            String line;
            while ((line = br.readLine()) != null) {
                // Diviser la ligne en colonnes en utilisant une expression régulière qui gère les virgules dans les valeurs
                String[] data = line.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)", -1);

                // Vérifier si la ligne contient suffisamment de colonnes
                if (data.length >= 5) {
                    String brandName = data[3].replaceAll("\"", "").trim();

                    // Insérer les données dans la table dim_transaction uniquement si elles n'existent pas déjà
                    pstmt.setString(1, brandName);
                    pstmt.setString(2, brandName);
                    pstmt.executeUpdate();
                } else {
                    System.err.println("La ligne ne contient pas suffisamment de colonnes : " + line);
                }
            }
            br.close();

            System.out.println("Chargement des données terminé avec succès.");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        String csvFilePath = "C:\\Users\\the cast\\OneDrive\\Bureau\\POS_DATA\\fichier_segmentation_Nettoyé.csv";
        loadUserData(csvFilePath);
    }
}
