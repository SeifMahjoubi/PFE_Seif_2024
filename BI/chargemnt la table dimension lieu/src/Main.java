import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class Main {
    public static void main(String[] args) {
        String csvFile = "C:\\Users\\the cast\\OneDrive\\Bureau\\POS_DATA\\POS_segmentation_POS_Profil.csv";
        String jdbcURL = "jdbc:postgresql://localhost:5432/djezzy";
        String username = "postgres";
        String password = "postgres";

        try (Connection connection = DriverManager.getConnection(jdbcURL, username, password)) {
            String line = "";
            String cvsSplitBy = "\t"; // Séparateur de champ du fichier CSV (tabulation)
            try (BufferedReader br = new BufferedReader(new FileReader(csvFile))) {
                br.readLine();
                // Préparer la requête d'insertion
                String insertQuery = "INSERT INTO dim_lieu_geographique (region, wilaya, commune) " +
                        "SELECT DISTINCT ? AS region, ? AS wilaya, ? AS commune " +
                        "WHERE NOT EXISTS " +
                        "(SELECT 1 FROM dim_lieu_geographique WHERE region = ? AND wilaya = ? AND commune = ?)";
                try (PreparedStatement preparedStatement = connection.prepareStatement(insertQuery)) {
                    int totalRowsInserted = 0;
                    while ((line = br.readLine()) != null) {
                        String[] data = line.split(cvsSplitBy);
                        if (data.length < 4) {
                            System.out.println("La ligne suivante est mal formée et sera ignorée : " + line);
                            continue;}
                        String region = data[1].trim();
                        String wilaya = data[2].trim();
                        String commune = data[3].trim();
                        preparedStatement.setString(1, region);
                        preparedStatement.setString(2, wilaya);
                        preparedStatement.setString(3, commune);
                        preparedStatement.setString(4, region);
                        preparedStatement.setString(5, wilaya);
                        preparedStatement.setString(6, commune);
                        int rowsInserted = preparedStatement.executeUpdate();
                        totalRowsInserted += rowsInserted;}
                    System.out.println("Nombre total de lignes insérées dans la table dim_lieu_geographique : " + totalRowsInserted);
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            } catch (IOException e) {
                e.printStackTrace();}

        } catch (SQLException e) { e.printStackTrace();
    }}}

