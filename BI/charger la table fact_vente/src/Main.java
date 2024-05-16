import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;

public class Main {

    public static void main(String[] args) {
        String jdbcURL = "jdbc:postgresql://localhost:5432/djezzy";
        String username = "postgres";
        String password = "postgres";
        try (Connection connection = DriverManager.getConnection(jdbcURL, username, password)) {
            System.out.println("Connexion établie avec succès !");

            String csvFile = "C:/Users/the cast/OneDrive/Bureau/POS_DATA/segmentation_csv.csv";

            try (BufferedReader reader = new BufferedReader(new FileReader(csvFile))) {
                String line = reader.readLine(); // Ignorer la première ligne (en-têtes)

                while ((line = reader.readLine()) != null) {
                    String[] parts = line.split(",");
                    String code_pdv = parts[0];
                    String transaction_date = parts[1];
                    String flexy_amount = parts[2].trim().replace(",", ".");
                    String brand_name = parts[3];
                    String region = parts[4];
                    String wilaya = parts[5];
                    String commune = parts[6];

                    int id_dim_pdv = getIdFromDimPointDeVente(connection, code_pdv);
                    int id_dim_transaction = getIdFromDimTransaction(connection, brand_name);
                    int id_dim_date_transaction = getIdFromDimDateTransaction(connection, transaction_date);
                    int id_dim_lieu_geo = getIdFromDimLieuGeographique(connection, region, wilaya, commune);

                    if (id_dim_pdv != -1 && id_dim_transaction != -1 && id_dim_date_transaction != -1 && id_dim_lieu_geo != -1) {
                        insertFactVente(connection, id_dim_pdv, Float.parseFloat(flexy_amount), id_dim_transaction, id_dim_date_transaction, id_dim_lieu_geo);
                        System.out.println("Données insérées avec succès !");
                    } else {
                        System.out.println("Une ou plusieurs clés étrangères ne correspondent pas.");
                    }
                }
                System.out.println("Table chargée avec succès !");
            } catch (IOException e) {
                System.out.println("Erreur lors de la lecture du fichier CSV : " + e.getMessage());
            }

        } catch (SQLException e) {
            System.out.println("Erreur lors de la connexion à la base de données: " + e.getMessage());
        }
    }

    private static int getIdFromDimPointDeVente(Connection connection, String code_pdv) throws SQLException {
        String query = "SELECT id_dim_pdv FROM dim_point_de_vente WHERE code_pdv = ?";
        try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setString(1, code_pdv);
            try (var resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getInt("id_dim_pdv");
                }
            }
        }
        return -1;
    }

    private static int getIdFromDimTransaction(Connection connection, String brand_name) throws SQLException {
        String query = "SELECT id_dim_transaction FROM dim_transaction WHERE brand_name = ?";
        try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setString(1, brand_name);
            try (var resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getInt("id_dim_transaction");
                }
            }
        }
        return -1;
    }

    private static int getIdFromDimDateTransaction(Connection connection, String transaction_date) throws SQLException {
        String query = "SELECT id_dim_date_tran FROM dim_date_transaction WHERE date_transaction = ?";
        try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            // Convertir la chaîne de caractères en un objet Timestamp
            Timestamp timestamp = convertStringToTimestamp(transaction_date);
            preparedStatement.setTimestamp(1, timestamp);
            try (var resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getInt("id_dim_date_tran");
                }
            }
        }
        return -1;
    }

    private static int getIdFromDimLieuGeographique(Connection connection, String region, String wilaya, String commune) throws SQLException {
        String query = "SELECT id_dim_lieu_geo FROM dim_lieu_geographique WHERE region = ? AND wilaya = ? AND commune = ?";
        try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setString(1, region);
            preparedStatement.setString(2, wilaya);
            preparedStatement.setString(3, commune);
            try (var resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getInt("id_dim_lieu_geo");
                }
            }
        }
        return -1;
    }

    private static void insertFactVente(Connection connection, int id_dim_pdv, float flexy_amount, int id_dim_transaction, int id_dim_date_transaction, int id_dim_lieu_geo) throws SQLException {
        String query = "INSERT INTO fact_vente (id_dim_pdv, flexy_amount, id_dim_transaction, id_dim_date_tran, id_dim_lieu_geo) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setInt(1, id_dim_pdv);
            preparedStatement.setFloat(2, flexy_amount);
            preparedStatement.setInt(3, id_dim_transaction);
            preparedStatement.setInt(4, id_dim_date_transaction);
            preparedStatement.setInt(5, id_dim_lieu_geo);
            preparedStatement.executeUpdate();
        }
    }

    private static Timestamp convertStringToTimestamp(String transaction_date) {
        try {
            // Définir le format de la chaîne de caractères de date
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            // Convertir la chaîne de caractères en un objet Timestamp
            return new Timestamp(dateFormat.parse(transaction_date).getTime());
        } catch (ParseException e) {
            e.printStackTrace();
            return null; // Gérer les erreurs de conversion
        }
    }
}
