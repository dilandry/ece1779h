package ca.utoronto.ece1779.database;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;

public class DeleteAll {
    private static final String ACCESS_KEY = "figure out yourself :)";
    private static final String SECRET_KEY = "so so secret";
    private static final String BUCKET = "ece1779-group26";

    private static final BasicAWSCredentials awsCreds = new BasicAWSCredentials(ACCESS_KEY, SECRET_KEY);
    private static final AmazonS3 s3Client = new AmazonS3Client(awsCreds);
    // Oregon region
    private static final Region usWest = Region.getRegion(Regions.US_WEST_2);

    static {
        s3Client.setRegion(usWest);
    }

    public static void deleteAll() {
        deleteS3Data();
        deleteDataInDatabase();
    }

    public static void deleteS3Data() {
        s3Client.deleteBucket(BUCKET);
        s3Client.createBucket(BUCKET);
    }

    public static void deleteDataInDatabase() {
        String deleteImages = "Delete FROM images";
        String deleteUsers = "Delete FROM users";
        Connection con = DatabaseConnection.getInstance().getConnection();

        try {
            Statement stmt = con.createStatement();
            stmt.execute(deleteImages);
            stmt.execute(deleteUsers);
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            closeConnection(con);
        }
    }

    private static void closeConnection(Connection con) {
        try {
            con.close();
        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("Could not close connection");
        }
    }
}
