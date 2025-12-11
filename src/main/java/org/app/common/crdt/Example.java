package org.app.common.crdt;

public class Example {
    // ========== USAGE EXAMPLES ==========


    /**
     * Example 1: Web Analytics - Page Views Counter
     */
    public static void webAnalyticsExample() {
        EasyCRDT analytics = new EasyCRDT("analytics-server");

        // Track page views (grow-only counter)
        var pageViews = analytics.createGrowCounter("homepage-views");
        pageViews.increment(); // User visits page
        pageViews.increment(5); // 5 more visits

        System.out.println("Homepage views: " + pageViews.getValue());
    }

    /**
     * Example 2: E-commerce - Shopping Cart
     */
    public static void shoppingCartExample() {
        EasyCRDT userDevice = new EasyCRDT("user-mobile");

        // Shopping cart items (add-remove set)
        var cart = userDevice.createAddRemoveSet("shopping-cart");
        cart.add("laptop");
        cart.add("mouse");
        cart.add("keyboard");
        cart.remove("mouse"); // Changed mind

        System.out.println("Cart items: " + cart.getAll());
        System.out.println("Cart size: " + cart.size());
    }

    /**
     * Example 3: Social Media - Like/Dislike System
     */
    public static void socialMediaExample() {
        EasyCRDT socialApp = new EasyCRDT("social-server");

        // Post likes/dislikes (plus-minus counter)
        var postScore = socialApp.createPlusMinusCounter("post-123-score");
        postScore.increment(); // Like
        postScore.increment(); // Another like
        postScore.decrement(); // Dislike

        System.out.println("Post score: " + postScore.getValue());
    }

    /**
     * Example 4: User Profile - Last Updated Info
     */
    public static void userProfileExample() {
        EasyCRDT userApp = new EasyCRDT("user-app");

        // User profile data (last-writer-wins register)
        var userName = userApp.createRegister("user-123-name");
        userName.set("John Doe");
        userName.set("Johnny Doe"); // Updated name

        System.out.println("User name: " + userName.get().orElse("Unknown"));
    }

    /**
     * Example 5: Multi-Node Sync Scenario
     */
    public static void multiNodeSyncExample() {
        // Two different nodes
        EasyCRDT node1 = new EasyCRDT("server-1");
        EasyCRDT node2 = new EasyCRDT("server-2");

        // Same counter on both nodes
        var counter1 = node1.createGrowCounter("global-counter");
        var counter2 = node2.createGrowCounter("global-counter");

        // Operations on different nodes
        counter1.increment(10);
        counter2.increment(5);

        // Sync between nodes
        node1.syncWith(node2);

        System.out.println("Node 1 counter: " + counter1.getValue());
        System.out.println("Node 2 counter: " + counter2.getValue());
    }

    /**
     * Example 6: Real-time Collaborative Tags
     */
    public static void collaborativeTagsExample() {
        EasyCRDT editor1 = new EasyCRDT("editor-alice");
        EasyCRDT editor2 = new EasyCRDT("editor-bob");

        // Document tags (grow-only set)
        var tags1 = editor1.createGrowSet("document-tags");
        var tags2 = editor2.createGrowSet("document-tags");

        // Different users add tags
        tags1.add("java");
        tags1.add("tutorial");
        tags2.add("programming");
        tags2.add("crdt");

        // After sync, both will have all tags
        editor1.syncWith(editor2);

        System.out.println("All tags: " + tags1.getAll());
    }

    public static void test(String[] args) {
        System.out.println("=== Web Analytics Example ===");
        webAnalyticsExample();

        System.out.println("\n=== Shopping Cart Example ===");
        shoppingCartExample();

        System.out.println("\n=== Social Media Example ===");
        socialMediaExample();

        System.out.println("\n=== User Profile Example ===");
        userProfileExample();

        System.out.println("\n=== Multi-Node Sync Example ===");
        multiNodeSyncExample();

        System.out.println("\n=== Collaborative Tags Example ===");
        collaborativeTagsExample();
    }
}
