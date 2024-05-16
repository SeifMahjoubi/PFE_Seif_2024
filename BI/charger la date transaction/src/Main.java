import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class Main {
    public static void main(String[] args) {
        String csvFilePath = "C:\\Users\\the cast\\OneDrive\\Bureau\\POS_DATA\\fichier_segmentation_Nettoyé.csv";
        String jdbcURL = "jdbc:postgresql://localhost:5432/djezzy";
        String username = "postgres";
        String password = "postgres";

        int totalRowsInserted = 0;

        try (Connection connection = DriverManager.getConnection(jdbcURL, username, password)) {
            String line = "";

            try (BufferedReader br = new BufferedReader(new FileReader(csvFilePath))) {
                br.readLine(); // Ignorer la première ligne (en-têtes)

                while ((line = br.readLine()) != null) {
                    String[] data = line.split(","); // Diviser la ligne en fonction de la virgule

                    // Extraire la date de la transaction
                    String transactionDate = data[1]; // La deuxième colonne contient la date de la transaction

                    try {
                        // Convertir la date de la colonne Transaction_date en timestamp
                        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                        Calendar calendar = Calendar.getInstance();
                        calendar.setTime(sdf.parse(transactionDate)); // Convertir la date en objet Calendar
                        // Insérer les valeurs dans la table dim_date_transaction si elles n'existent pas déjà
                        String insertQuery = "INSERT INTO dim_date_transaction (date_transaction, jour, mois, nommois, nomjour, annee) " +
                                "SELECT ?, ?, ?, ?, ?, ? " +
                                "WHERE NOT EXISTS " +
                                "(SELECT 1 FROM dim_date_transaction WHERE date_transaction = ?)";
                        try (PreparedStatement preparedStatement = connection.prepareStatement(insertQuery)) {
                            preparedStatement.setTimestamp(1, new Timestamp(calendar.getTimeInMillis())); // date_transaction
                            preparedStatement.setInt(2, calendar.get(Calendar.DAY_OF_MONTH)); // jour
                            preparedStatement.setInt(3, calendar.get(Calendar.MONTH) + 1); // mois
                            preparedStatement.setString(4, new SimpleDateFormat("MMMM").format(calendar.getTime())); // nommois
                            preparedStatement.setString(5, new SimpleDateFormat("EEEE").format(calendar.getTime())); // nomjour
                            preparedStatement.setInt(6, calendar.get(Calendar.YEAR)); // annee
                            preparedStatement.setTimestamp(7, new Timestamp(calendar.getTimeInMillis())); // Date pour la vérification

                            int rowsInserted = preparedStatement.executeUpdate();
                            totalRowsInserted += rowsInserted;
                        } catch (SQLException e) {
                            e.printStackTrace();
                        }
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

            System.out.println("Total lignes insérées : " + totalRowsInserted);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
