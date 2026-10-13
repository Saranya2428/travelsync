package travelsync;

import java.sql.*;
import java.util.*;
import java.security.SecureRandom;
import java.security.MessageDigest;
import java.security.spec.KeySpec;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

import travelsync.Main.User;

public class DB {

    private static final String URL =
            "jdbc:postgresql://localhost:5432/travelsync";
    private static final String DB_USER = "postgres";
    private static final String DB_PASSWORD = "karthik2428";

    private static Connection connect() throws SQLException {
        return DriverManager.getConnection(URL, DB_USER, DB_PASSWORD);
    }

    public static Connection initialize() throws SQLException {
        Connection con = connect();

        try (Statement st = con.createStatement()) {
            st.execute("""
                CREATE TABLE IF NOT EXISTS app_users (
                    id SERIAL PRIMARY KEY,
                    username VARCHAR(24) UNIQUE NOT NULL,
                    password_hash TEXT NOT NULL,
                    role VARCHAR(5) NOT NULL
                        CHECK (role IN ('ADMIN', 'USER'))
                )
            """);

            st.execute("""
                CREATE TABLE IF NOT EXISTS destinations (
                    id SERIAL PRIMARY KEY,
                    name VARCHAR(100) NOT NULL,
                    hotel VARCHAR(100),
                    attraction VARCHAR(150),
                    km INTEGER NOT NULL CHECK (km >= 0)
                )
            """);
        } catch (SQLException e) {
            con.close();
            throw e;
        }

        seedDestinations();
        System.out.println("TravelSync database initialized.");
        return con;
    }

    // Password hashing
    private static String hashPassword(String password)
            throws Exception {

        byte[] salt = new byte[16];
        new SecureRandom().nextBytes(salt);

        KeySpec spec = new PBEKeySpec(
                password.toCharArray(), salt, 65536, 256);

        byte[] hash = SecretKeyFactory
                .getInstance("PBKDF2WithHmacSHA256")
                .generateSecret(spec)
                .getEncoded();

        return Base64.getEncoder().encodeToString(salt)
                + ":" +
               Base64.getEncoder().encodeToString(hash);
    }

    private static boolean verifyPassword(
            String password, String stored) throws Exception {

        String[] parts = stored.split(":");
        if (parts.length != 2) return false;

        byte[] salt = Base64.getDecoder().decode(parts[0]);
        byte[] expected = Base64.getDecoder().decode(parts[1]);

        KeySpec spec = new PBEKeySpec(
                password.toCharArray(), salt, 65536, 256);

        byte[] actual = SecretKeyFactory
                .getInstance("PBKDF2WithHmacSHA256")
                .generateSecret(spec)
                .getEncoded();

        return MessageDigest.isEqual(expected, actual);
    }

    // Create a new normal user
    public static void register(String username, String password)
            throws Exception {

        String sql = """
            INSERT INTO app_users(username, password_hash, role)
            VALUES (?, ?, 'USER')
        """;

        try (Connection con = connect();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, username);
            ps.setString(2, hashPassword(password));
            ps.executeUpdate();
        }
    }

    // Login
    public static User login(
            String username, String password, String role) {

        String sql = """
            SELECT id, username, password_hash, role
            FROM app_users
            WHERE username = ? AND role = ?
        """;

        try (Connection con = connect();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, username.trim());
            ps.setString(2, role.trim().toUpperCase());

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()
                        && verifyPassword(
                            password, rs.getString("password_hash"))) {

                    return new User(
                            rs.getInt("id"),
                            rs.getString("username"),
                            rs.getString("role"));
                }
            }
        } catch (Exception e) {
            System.out.println("Login database error: "
                    + e.getMessage());
        }

        return null;
    }

    // Create the first admin account only
    public static void admin(String username, String password)
            throws Exception {

        try (Connection con = connect()) {
            con.setAutoCommit(false);

            try {
                // Serialize admin setup so two first admins cannot
                // be created concurrently.
                try (Statement st = con.createStatement()) {
                    st.execute(
                        "LOCK TABLE app_users IN EXCLUSIVE MODE");
                }

                try (PreparedStatement check = con.prepareStatement(
                        "SELECT COUNT(*) FROM app_users WHERE role='ADMIN'");
                     ResultSet rs = check.executeQuery()) {

                    rs.next();
                    if (rs.getInt(1) > 0) {
                        throw new SQLException(
                                "An admin account already exists.");
                    }
                }

                try (PreparedStatement ps = con.prepareStatement("""
                    INSERT INTO app_users(username, password_hash, role)
                    VALUES (?, ?, 'ADMIN')
                """)) {
                    ps.setString(1, username);
                    ps.setString(2, hashPassword(password));
                    ps.executeUpdate();
                }

                con.commit();

            } catch (Exception e) {
                con.rollback();
                throw e;
            } finally {
                con.setAutoCommit(true);
            }
        }
    }

    // Destination model
    public static class Destination {
        public int id;
        public String name;
        public String hotel;
        public String attraction;
        public int km;

        public Destination(int id, String name, String hotel,
                           String attraction, int km) {
            this.id = id;
            this.name = name;
            this.hotel = hotel;
            this.attraction = attraction;
            this.km = km;
        }
    }

    // View all destinations
    public static List<Destination> destinations()
            throws SQLException {

        List<Destination> list = new ArrayList<>();

        String sql = """
            SELECT id, name, hotel, attraction, km
            FROM destinations
            ORDER BY id
        """;

        try (Connection con = connect();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                list.add(new Destination(
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getString("hotel"),
                        rs.getString("attraction"),
                        rs.getInt("km")));
            }
        }

        return list;
    }

    // Find a destination by ID
    public static Destination get(int id) throws SQLException {
        String sql = """
            SELECT id, name, hotel, attraction, km
            FROM destinations WHERE id = ?
        """;

        try (Connection con = connect();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, id);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new Destination(
                            rs.getInt("id"),
                            rs.getString("name"),
                            rs.getString("hotel"),
                            rs.getString("attraction"),
                            rs.getInt("km"));
                }
            }
        }

        return null;
    }

    // Add or update a destination
    public static void save(int id, String name, String hotel,
                            String attraction, int km)
            throws SQLException {

        if (id == 0) {
            String sql = """
                INSERT INTO destinations(name, hotel, attraction, km)
                VALUES (?, ?, ?, ?)
            """;

            try (Connection con = connect();
                 PreparedStatement ps = con.prepareStatement(sql)) {

                ps.setString(1, name);
                ps.setString(2, hotel);
                ps.setString(3, attraction);
                ps.setInt(4, km);
                ps.executeUpdate();
            }

        } else {
            String sql = """
                UPDATE destinations
                SET name = ?, hotel = ?, attraction = ?, km = ?
                WHERE id = ?
            """;

            try (Connection con = connect();
                 PreparedStatement ps = con.prepareStatement(sql)) {

                ps.setString(1, name);
                ps.setString(2, hotel);
                ps.setString(3, attraction);
                ps.setInt(4, km);
                ps.setInt(5, id);

                if (ps.executeUpdate() == 0) {
                    throw new SQLException("Destination not found.");
                }
            }
        }
    }

    // Delete a destination
    public static void delete(int id) throws SQLException {
        try (Connection con = connect();
             PreparedStatement ps = con.prepareStatement(
                     "DELETE FROM destinations WHERE id = ?")) {

            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    // List accounts for the admin
    public static List<Object[]> users() throws SQLException {
        List<Object[]> list = new ArrayList<>();

        try (Connection con = connect();
             PreparedStatement ps = con.prepareStatement("""
                 SELECT id, username, role
                 FROM app_users ORDER BY id
             """);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                list.add(new Object[] {
                    rs.getInt("id"),
                    rs.getString("username"),
                    rs.getString("role")
                });
            }
        }

        return list;
    }

    // Insert sample destinations only when the table is empty
    private static void seedDestinations() throws SQLException {
        String sql = """
            INSERT INTO destinations(name, hotel, attraction, km)
            SELECT ?, ?, ?, ?
            WHERE NOT EXISTS (SELECT 1 FROM destinations)
        """;

        try (Connection con = connect();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, "Tirupati");
            ps.setString(2, "Hotel Bliss");
            ps.setString(3, "Tirumala Temple");
            ps.setInt(4, 410);
            ps.executeUpdate();
        }
    }

    public static void main(String[] args) {
        try (Connection con = initialize()) {
            System.out.println("PostgreSQL connected successfully!");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}