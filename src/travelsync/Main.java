
package travelsync;

import java.util.*;

public class Main {

    static final Scanner in = new Scanner(System.in);

    static class User {
        int id;
        String username, role;

        User(int id, String username, String role) {
            this.id = id;
            this.username = username;
            this.role = role;
        }

        boolean admin() {
            return "ADMIN".equalsIgnoreCase(role);
        }
    }

    public static void main(String[] args) {
        try {
            DB.initialize();

            while (true) {
                System.out.println("\n==============================================");
                System.out.println(" TravelSync — Intelligent Travel Planning Platform");
                System.out.println("==============================================");
                System.out.println("1. Login");
                System.out.println("2. Register User");
                System.out.println("0. Exit");

                switch (num("Choose: ")) {
                    case 1 -> login();
                    case 2 -> register();
                    case 0 -> {
                        System.out.println("Thank you for using TravelSync!");
                        return;
                    }
                    default -> System.out.println("Invalid choice.");
                }
            }
        } catch (Exception e) {
            System.out.println("Startup error: " + e.getMessage());
            System.out.println("Check DB.java, PostgreSQL, and JDBC configuration.");
        }
    }

    static int num(String prompt) {
        while (true) {
            System.out.print(prompt);
            try {
                return Integer.parseInt(in.nextLine().trim());
            } catch (NumberFormatException e) {
                System.out.println("Enter a valid number.");
            }
        }
    }

    static String ask(String prompt) {
        System.out.print(prompt);
        return in.nextLine();
    }

    // ---------------- LOGIN ----------------

    static void login() {
        String username = ask("Username: ").trim();
        String password = ask("Password: ");
        String role = ask("Role (ADMIN/USER): ").trim().toUpperCase();

        if (!role.equals("ADMIN") && !role.equals("USER")) {
            System.out.println("Role must be ADMIN or USER.");
            return;
        }

        User user = DB.login(username, password, role);

        if (user == null) {
            System.out.println("Invalid username/password/role.");
            return;
        }

        System.out.println("\nWelcome, " + user.username + "!");
        travelSyncMenu(user);
    }

    static void register() {
        String username = ask("Username (3-24 letters/numbers/_): ").trim();
        String password = ask("Password (8+ characters): ");

        if (!username.matches("[A-Za-z0-9_]{3,24}")
                || password.length() < 8) {
            System.out.println("Invalid username or password format.");
            return;
        }

        try {
            DB.register(username, password);
            System.out.println("Registration successful! Account role: USER");
            System.out.println("You can now log in using role USER.");
        } catch (Exception e) {
            System.out.println("Registration failed: " + e.getMessage());
        }
    }

    // ---------------- TRAVELSYNC MAIN MENU ----------------

    static void travelSyncMenu(User user) {
        while (true) {
            System.out.println("\n==================================================");
            System.out.println(" TravelSync — Intelligent Travel Planning Platform");
            System.out.println(" DSA3 Advanced Algorithms Demonstration (Java)");
            System.out.println("==================================================");
            System.out.println("Logged in: " + user.username + " (" + user.role + ")");

            System.out.println("\nMAIN MENU");
            System.out.println("1. Search destinations/hotels (KMP + Z + Rabin-Karp)");
            System.out.println("2. Fuzzy destination matching (edit distance)");
            System.out.println("3. Show suffix array and LCP");
            System.out.println("4. Plan a route (shortest path)");
            System.out.println("5. Transportation capacity / max-flow");
            System.out.println("6. Bipartite matching (guides to tours)");
            System.out.println("7. Approximate multi-stop trip (TSP bitmask DP)");
            System.out.println("8. Vertex-cover approximation");
            System.out.println("9. Randomised algorithms (quicksort + primality test)");
            System.out.println("10. Streaming booking sample (reservoir sampling)");
            System.out.println("11. Parallel reduction demo");
            System.out.println("12. View all sample destinations");

            if (user.admin()) {
                System.out.println("13. Admin destination management");
            }

            System.out.println("0. Logout");

            int choice = num("Choose: ");

            try {
                switch (choice) {
                    case 1 -> searchAll();
                    case 2 -> fuzzy();
                    case 3 -> suffixArrayDemo();
                    case 4 -> System.out.println(Algorithms.route());
                    case 5 -> System.out.println(
                            "Maximum flow: " + Algorithms.flow());
                    case 6 -> bipartiteDemo();
                    case 7 -> System.out.println(Algorithms.tsp());
                    case 8 -> vertexCoverDemo();
                    case 9 -> randomizedDemo();
                    case 10 -> reservoirDemo();
                    case 11 -> parallelReductionDemo();
                    case 12 -> show(DB.destinations());
                    case 13 -> {
                        if (user.admin()) {
                            adminMenu();
                        } else {
                            System.out.println("Admin access required.");
                        }
                    }
                    case 0 -> {
                        System.out.println("Logged out successfully.");
                        return;
                    }
                    default -> System.out.println("Invalid choice.");
                }
            } catch (Exception e) {
                System.out.println("Operation failed: " + e.getMessage());
            }
        }
    }

    // ---------------- ADMIN MENU ----------------

    static void adminMenu() throws Exception {
        while (true) {
            System.out.println("\n--- ADMIN DESTINATION MANAGEMENT ---");
            System.out.println("1. View destinations");
            System.out.println("2. Add destination");
            System.out.println("3. Update destination");
            System.out.println("4. Delete destination");
            System.out.println("5. View accounts");
            System.out.println("0. Back to TravelSync menu");

            switch (num("Choose: ")) {
                case 1 -> show(DB.destinations());
                case 2 -> save(0);
                case 3 -> save(num("Destination ID to update: "));
                case 4 -> deleteDestination();
                case 5 -> {
                    for (Object[] account : DB.users()) {
                        System.out.println(Arrays.toString(account));
                    }
                }
                case 0 -> {
                    return;
                }
                default -> System.out.println("Invalid choice.");
            }
        }
    }

    // ---------------- DESTINATIONS ----------------

    static void show(List<DB.Destination> destinations) {
        if (destinations.isEmpty()) {
            System.out.println("No destinations found.");
            return;
        }

        System.out.printf("%-5s %-22s %-22s %-25s %s%n",
                "ID", "Destination", "Hotel", "Attraction", "KM");

        for (DB.Destination d : destinations) {
            System.out.printf("%-5d %-22s %-22s %-25s %d%n",
                    d.id, d.name, d.hotel, d.attraction, d.km);
        }
    }

    static void searchAll() throws Exception {
        String query = ask("Enter destination/hotel/attraction keyword: ")
                .trim().toLowerCase();

        if (query.isEmpty()) {
            System.out.println("Search keyword cannot be empty.");
            return;
        }

        List<DB.Destination> results = new ArrayList<>();

        System.out.println("\n--- Searching with KMP, Z and Rabin-Karp ---");

        for (DB.Destination d : DB.destinations()) {
            String text = (d.name + " " + d.hotel + " " + d.attraction)
                    .toLowerCase();

            boolean foundKMP = Algorithms.kmpContains(text, query);
            boolean foundZ = zContains(text, query);
            boolean foundRK = rabinKarpContains(text, query);

            if (foundKMP || foundZ || foundRK) {
                results.add(d);
                System.out.println("Matched: " + d.name
                        + " [KMP=" + foundKMP
                        + ", Z=" + foundZ
                        + ", Rabin-Karp=" + foundRK + "]");
            }
        }

        if (results.isEmpty()) {
            System.out.println("No exact substring match found.");
            System.out.println("Try option 2 for fuzzy matching.");
        } else {
            System.out.println("\n--- Search Results ---");
            show(results);
        }
    }

    static boolean zContains(String text, String pattern) {
        if (pattern.isEmpty()) return true;

        String combined = pattern + "$" + text;
        int[] z = new int[combined.length()];
        int left = 0, right = 0;

        for (int i = 1; i < combined.length(); i++) {
            if (i <= right) {
                z[i] = Math.min(right - i + 1, z[i - left]);
            }

            while (i + z[i] < combined.length()
                    && combined.charAt(z[i])
                    == combined.charAt(i + z[i])) {
                z[i]++;
            }

            if (i + z[i] - 1 > right) {
                left = i;
                right = i + z[i] - 1;
            }

            if (z[i] >= pattern.length()) return true;
        }

        return false;
    }

    static boolean rabinKarpContains(String text, String pattern) {
        if (pattern.isEmpty()) return true;
        if (pattern.length() > text.length()) return false;

        long base = 256;
        long mod = 1_000_000_007L;
        long patternHash = 0;
        long textHash = 0;
        long power = 1;

        for (int i = 0; i < pattern.length() - 1; i++) {
            power = (power * base) % mod;
        }

        for (int i = 0; i < pattern.length(); i++) {
            patternHash = (patternHash * base + pattern.charAt(i)) % mod;
            textHash = (textHash * base + text.charAt(i)) % mod;
        }

        for (int i = 0; i <= text.length() - pattern.length(); i++) {
            if (patternHash == textHash
                    && text.regionMatches(i, pattern, 0, pattern.length())) {
                return true;
            }

            if (i < text.length() - pattern.length()) {
                textHash = (textHash
                        - text.charAt(i) * power % mod + mod) % mod;
                textHash = (textHash * base
                        + text.charAt(i + pattern.length())) % mod;
            }
        }

        return false;
    }

    static void fuzzy() throws Exception {
        String query = ask("Destination spelling: ").trim().toLowerCase();
        if (query.isEmpty()) {
            System.out.println("Enter a destination name.");
            return;
        }

        String best = "";
        int bestDistance = Integer.MAX_VALUE;

        for (DB.Destination d : DB.destinations()) {
            int distance = Algorithms.distance(
                    query, d.name.toLowerCase());

            if (distance < bestDistance) {
                bestDistance = distance;
                best = d.name;
            }
        }

        if (best.isEmpty()) {
            System.out.println("No destinations available.");
        } else {
            System.out.println("Closest destination: " + best);
            System.out.println("Edit distance: " + bestDistance);
        }
    }

    static void save(int id) throws Exception {
        DB.Destination old = id == 0 ? null : DB.get(id);

        if (id != 0 && old == null) {
            System.out.println("Destination ID not found.");
            return;
        }

        String name = ask("Destination name"
                + (old == null ? "" : " [" + old.name + "]") + ": ").trim();

        String hotel = ask("Hotel"
                + (old == null ? "" : " [" + old.hotel + "]") + ": ").trim();

        String attraction = ask("Attraction"
                + (old == null ? "" : " [" + old.attraction + "]") + ": ").trim();

        int km = num("Distance in KM"
                + (old == null ? "" : " [" + old.km + "]") + ": ");

        if (old != null) {
            if (name.isEmpty()) name = old.name;
            if (hotel.isEmpty()) hotel = old.hotel;
            if (attraction.isEmpty()) attraction = old.attraction;
            if (km == 0) km = old.km;
        }

        if (name.isEmpty() || km < 0) {
            System.out.println("Name is required and distance cannot be negative.");
            return;
        }

        DB.save(id, name, hotel, attraction, km);
        System.out.println("Destination saved successfully.");
    }

    static void deleteDestination() throws Exception {
        int id = num("Destination ID: ");
        DB.Destination d = DB.get(id);

        if (d == null) {
            System.out.println("Destination not found.");
            return;
        }

        if (ask("Delete " + d.name + "? (yes/no): ")
                .equalsIgnoreCase("yes")) {
            DB.delete(id);
            System.out.println("Destination deleted.");
        } else {
            System.out.println("Deletion cancelled.");
        }
    }

    // ---------------- SUFFIX ARRAY + LCP ----------------

    static void suffixArrayDemo() {
        String text = ask("Enter text: ");

        if (text.isEmpty()) {
            System.out.println("Text cannot be empty.");
            return;
        }

        Integer[] suffixes = new Integer[text.length()];
        for (int i = 0; i < text.length(); i++) {
            suffixes[i] = i;
        }

        Arrays.sort(suffixes,
                Comparator.comparing(text::substring));

        System.out.println("\nSuffix Array:");
        for (int i : suffixes) {
            System.out.println(i + " : " + text.substring(i));
        }

        System.out.println("\nLCP Array:");
        System.out.print("0 ");

        for (int i = 1; i < suffixes.length; i++) {
            System.out.print(lcp(
                    text.substring(suffixes[i - 1]),
                    text.substring(suffixes[i])) + " ");
        }

        System.out.println();
    }

    static int lcp(String a, String b) {
        int i = 0;
        while (i < a.length() && i < b.length()
                && a.charAt(i) == b.charAt(i)) {
            i++;
        }
        return i;
    }

    // ---------------- BIPARTITE MATCHING ----------------

    static void bipartiteDemo() {
        String[] guides = {"Guide-A", "Guide-B", "Guide-C"};
        String[] tours = {"Temple Tour", "Beach Tour", "City Tour"};

        int[][] graph = {
                {1, 1, 0},
                {1, 0, 1},
                {0, 1, 1}
        };

        int[] tourMatch = {-1, -1, -1};
        int matches = 0;

        for (int guide = 0; guide < guides.length; guide++) {
            boolean[] visited = new boolean[tours.length];

            if (matchGuide(guide, graph, visited, tourMatch)) {
                matches++;
            }
        }

        System.out.println("Guide-to-tour matching:");
        for (int t = 0; t < tourMatch.length; t++) {
            if (tourMatch[t] != -1) {
                System.out.println(guides[tourMatch[t]]
                        + " -> " + tours[t]);
            }
        }

        System.out.println("Maximum matching size: " + matches);
    }

    static boolean matchGuide(int guide, int[][] graph,
                              boolean[] visited, int[] tourMatch) {
        for (int tour = 0; tour < graph[guide].length; tour++) {
            if (graph[guide][tour] == 1 && !visited[tour]) {
                visited[tour] = true;

                if (tourMatch[tour] == -1
                        || matchGuide(tourMatch[tour],
                                graph, visited, tourMatch)) {
                    tourMatch[tour] = guide;
                    return true;
                }
            }
        }

        return false;
    }

    // ---------------- VERTEX COVER APPROXIMATION ----------------

    static void vertexCoverDemo() {
        int[][] edges = {
                {0, 1}, {0, 2}, {1, 2},
                {1, 3}, {2, 4}, {3, 4}
        };

        Set<Integer> cover = new LinkedHashSet<>();
        boolean[] covered = new boolean[edges.length];

        for (int i = 0; i < edges.length; i++) {
            if (!covered[i]) {
                int u = edges[i][0];
                int v = edges[i][1];

                cover.add(u);
                cover.add(v);

                for (int j = 0; j < edges.length; j++) {
                    if (edges[j][0] == u || edges[j][1] == u
                            || edges[j][0] == v || edges[j][1] == v) {
                        covered[j] = true;
                    }
                }
            }
        }

        System.out.println("Approximate vertex cover: " + cover);
        System.out.println("Number of selected vertices: " + cover.size());
    }

    // ---------------- RANDOMIZED ALGORITHMS ----------------

    static void randomizedDemo() {
        System.out.println("Randomized quicksort:");
        System.out.println(Algorithms.sortDemo());

        long n = num("Enter a positive integer for primality test: ");

        if (n < 2) {
            System.out.println(n + " is not prime.");
        } else {
            System.out.println(n + (isPrime(n)
                    ? " is prime."
                    : " is not prime."));
        }
    }

    // Trial-division primality check for demonstration
    static boolean isPrime(long n) {
        if (n < 2) return false;
        if (n == 2) return true;
        if (n % 2 == 0) return false;

        for (long i = 3; i <= n / i; i += 2) {
            if (n % i == 0) return false;
        }

        return true;
    }

    // ---------------- RESERVOIR SAMPLING ----------------

    static void reservoirDemo() {
        List<String> bookings = Arrays.asList(
                "Booking-101", "Booking-102", "Booking-103",
                "Booking-104", "Booking-105", "Booking-106",
                "Booking-107", "Booking-108");

        System.out.println("All sample bookings: " + bookings);
        System.out.println("Random sample of 3: "
                + Algorithms.sample(bookings, 3));
    }

    // ---------------- PARALLEL REDUCTION ----------------

    static void parallelReductionDemo() {
        List<Integer> distances = Arrays.asList(
                120, 250, 80, 410, 300, 150, 90);

        int sequentialSum = 0;
        for (int distance : distances) {
            sequentialSum += distance;
        }

        int parallelSum = distances.parallelStream()
                .mapToInt(Integer::intValue)
                .sum();

        System.out.println("Distances: " + distances);
        System.out.println("Sequential total: " + sequentialSum + " km");
        System.out.println("Parallel reduction total: " + parallelSum + " km");
    }
}
