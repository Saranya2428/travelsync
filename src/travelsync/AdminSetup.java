
package travelsync;

import java.sql.Connection;
import java.util.Scanner;

public class AdminSetup {

    public static void main(String[] args) {
        try (Scanner s = new Scanner(System.in)) {

            // Initialize the database
            try (Connection con = DB.initialize()) {
                System.out.println("Database ready.");
            }

            // Get admin credentials
            System.out.print("Admin username: ");
            String u = s.nextLine().trim();

            System.out.print("Admin password (8+ chars): ");
            String p = s.nextLine();

            // Validate input
            if (!u.matches("[A-Za-z0-9_]{3,24}")
                    || p.length() < 8) {
                System.out.println("Invalid credentials format.");
                return;
            }

            // Create the admin account
            DB.admin(u, p);

            System.out.println("Admin setup completed.");

        } catch (Exception e) {
            System.out.println(
                "Admin setup failed: " + e.getMessage()
            );
        }
    
}
}