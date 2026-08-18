package com.discover.discover_local_abilities_javaedition.service.helper;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Static reference data for activity types and context-based recommendation matching.
 *
 * For future improvement, we can:
 * - Consider adding more context keywords and activity types based on user feedback and data analysis.
 * - Implement a more dynamic way to update context-activity mappings without code changes,
 *   such as using a database or configuration file, or even leveraging machine learning to
 *   learn associations from user interactions and preferences.
 */
public final class Constants {

    private Constants() {
        // prevent instantiation
    }

    public static final List<String> ACTIVITY_TYPES = List.of(
            "art_gallery",
            "art_museum",
            "bakery",
            "bar",
            "barbecue_restaurant",
            "bistro",
            "bridge",
            "cafe",
            "cafeteria",
            "cake_shop",
            "church",
            "coffee_shop",
            "cultural_center",
            "dessert_restaurant",
            "donut_shop",
            "eastern_european_restaurant",
            "event_venue",
            "fast_food_restaurant",
            "gas_station",
            "gastropub",
            "hamburger_restaurant",
            "history_museum",
            "hotel",
            "irish_pub",
            "italian_restaurant",
            "lounge_bar",
            "meal_takeaway",
            "mosque",
            "movie_theater",
            "museum",
            "other",
            "pastry_shop",
            "pizza_restaurant",
            "playground",
            "restaurant",
            "seafood_restaurant",
            "soul_food_restaurant",
            "sushi_restaurant",
            "tourist_attraction",
            "wine_bar"
    );

    public static final double MIN_CATEGORY_RELEVANCE = 0.3;

    public static final Map<String, List<String>> CONTEXT_ACTIVITY_TYPES = Map.ofEntries(
            Map.entry("breakfast", List.of(
                    "breakfast_restaurant",
                    "brunch_restaurant",
                    "bakery",
                    "cafe",
                    "coffee_shop",
                    "pastry_shop",
                    "cake_shop",
                    "donut_shop",
                    "dessert_restaurant",
                    "dessert_shop"
            )),
            Map.entry("coffee", List.of(
                    "coffee_shop",
                    "cafe",
                    "bakery",
                    "pastry_shop",
                    "dessert_shop"
            )),
            Map.entry("lunch", List.of(
                    "lunch_restaurant",
                    "bistro",
                    "restaurant",
                    "sandwich_shop",
                    "cafeteria",
                    "fast_food_restaurant",
                    "hamburger_restaurant",
                    "pizza_restaurant",
                    "meal_takeaway",
                    "italian_restaurant",
                    "seafood_restaurant",
                    "barbecue_restaurant",
                    "soul_food_restaurant",
                    "diner",
                    "bakery",
                    "cafe",
                    "coffee_shop",
                    "pastry_shop",
                    "dessert_shop"
            )),
            Map.entry("dinner", List.of(
                    "fine_dining_restaurant",
                    "steak_house",
                    "seafood_restaurant",
                    "grill",
                    "bistro",
                    "italian_restaurant",
                    "eastern_european_restaurant",
                    "sushi_restaurant",
                    "barbecue_restaurant",
                    "restaurant",
                    "cafeteria"
            )),
            Map.entry("nightlife", List.of(
                    "night_club",
                    "cocktail_bar",
                    "bar",
                    "lounge",
                    "pub",
                    "gastropub",
                    "irish_pub",
                    "lounge_bar",
                    "wine_bar"
            )),
            Map.entry("culture", List.of(
                    "tourist_attraction",
                    "art_museum",
                    "art_gallery",
                    "museum",
                    "history_museum",
                    "cultural_center",
                    "bridge",
                    "monument",
                    "library",
                    "church",
                    "mosque"
            ))
    );

    public static final Map<String, Set<String>> CONTEXT_KEYWORDS = CONTEXT_ACTIVITY_TYPES.entrySet().stream()
            .collect(Collectors.toUnmodifiableMap(
                    Map.Entry::getKey,
                    entry -> Set.copyOf(entry.getValue())
            ));

    private static final Map<String, String> _DAY_ALIASES_MUTABLE = new java.util.HashMap<>();
    static {
        _DAY_ALIASES_MUTABLE.put("0", "monday");
        _DAY_ALIASES_MUTABLE.put("1", "tuesday");
        _DAY_ALIASES_MUTABLE.put("2", "wednesday");
        _DAY_ALIASES_MUTABLE.put("3", "thursday");
        _DAY_ALIASES_MUTABLE.put("4", "friday");
        _DAY_ALIASES_MUTABLE.put("5", "saturday");
        _DAY_ALIASES_MUTABLE.put("6", "sunday");
    }
    public static final Map<String, String> DAY_ALIASES = Collections.unmodifiableMap(_DAY_ALIASES_MUTABLE);
}
