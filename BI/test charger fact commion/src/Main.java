import java.io.BufferedReader;
import java.io.FileReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;

public class Main {
    // Informations de connexion à la base de données
    static final String URL = "jdbc:postgresql://localhost:5432/djezzy";
    static final String USER = "postgres";
    static final String PASSWORD = "postgres";

    // Méthode pour charger les données du fichier CSV dans la table dim_dates_comission
    public static void loadComissionData(String csvFilePath) {
        try {
            // Connexion à la base de données
            Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
            // Préparation de la requête SQL pour l'insertion des données
            PreparedStatement pstmt = conn.prepareStatement("INSERT INTO dim_dates_comission (date_comission, date_subscription) " +
                    "VALUES (?, ?) " +
                    "ON CONFLICT (date_comission, date_subscription) DO NOTHING");

            // Lecture du fichier CSV
            BufferedReader br = new BufferedReader(new FileReader(csvFilePath));
            String line;
            boolean firstLine = true;
            while ((line = br.readLine()) != null) {
                if (firstLine) {
                    firstLine = false;
                    continue; // Passer à la prochaine itération pour sauter la première ligne (en-tête)
                }

                // Diviser la ligne en colonnes en utilisant la virgule comme délimiteur (",")
                String[] data = line.split(",");

                // Vérifier si la ligne contient suffisamment de colonnes
                if (data.length >= 3) {
                    // Récupérer les colonnes Date_Commission et Date_Subscription
                    String dateCommissionStr = data[2].trim();
                    String dateSubscriptionStr = data[1].trim();

                    // Convertir les chaînes de caractères en objets Timestamp avec un format personnalisé
                    SimpleDateFormat dateFormatCommission = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
                    SimpleDateFormat dateFormatSubscription = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    Timestamp dateCommission = new Timestamp(dateFormatCommission.parse(dateCommissionStr).getTime());
                    Timestamp dateSubscription = new Timestamp(dateFormatSubscription.parse(dateSubscriptionStr).getTime());

                    // Insérer les données dans la table dim_dates_comission
                    pstmt.setTimestamp(1, dateCommission);
                    pstmt.setTimestamp(2, dateSubscription);
                    pstmt.executeUpdate();
                } else {
                    System.err.println("La ligne ne contient pas suffisamment de colonnes : " + line);
                }
            }

            // Fermer les ressources
            br.close();
            pstmt.close();
            conn.close();

            System.out.println("Chargement des données terminé avec succès.");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
 
    public static void main(String[] args) {
        // Chemin d'accès au fichier CSV
        String csvFilePath = "C:\\Users\\the cast\\OneDrive\\Bureau\\POS_DATA\\fichier_commission_Nettoyé.csv";
        // Appel de la méthode pour charger les données du fichier CSV
        loadComissionData(csvFilePath);
    }
}
