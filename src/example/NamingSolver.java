package example;

import java.util.HashMap;
import java.util.Map;

/**
 * Attempt to find the mapping "Firstname Lastname" to "Lastname, F.".
 */
public class NamingSolver
    extends StringSolver
{
    /**
     * Entry point.
     */
    public static void main(String... args)
    {
        final Map<String,String> mappings = new HashMap<>();

        mappings.put("Fred Flintstone", "Flintstone, F.");
        mappings.put("Barney Rubble",   "Rubble, B.");
        mappings.put("Road Runner",     "Runner, R.");
        mappings.put("Yosemite Sam",    "Sam, Y.");
        mappings.put("Elmer Fudd",      "Fudd, E.");
        mappings.put("Elvis Presley",   "Presley, E.");
        mappings.put("Bruce Wayne",     "Wayne, B.");

        new NamingSolver().solve(mappings);
    }
}
