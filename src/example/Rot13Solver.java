package example;

import java.util.HashMap;
import java.util.Map;

/**
 * Attempt to derive a simple run-length-encoding algorithm.
 *
 */
public class Rot13Solver
    extends StringSolver
{
    /**
     * Entry point.
     */
    public static void main(String... args)
    {
        final Map<String,String> mappings = new HashMap<>();
        for (int i=0; i < 200; i++) {
            String from = "";
            String to   = "";
            for (int j=0, c = 25 + (int)(Math.random() * 25);
                 j < c;
                 j++)
            {
                char f = (char)(' ' + ((int)('~' - ' ') * Math.random()));
                char t;
                if ('a' <= f && f <= 'z') {
                    t = (char)('a' + ((f - 'a') + 13) % 26);
                }
                else if ('A' <= f && f <= 'Z') {
                    t = (char)('A' + ((f - 'A') + 13) % 26);
                }
                else {
                    t = f;
                }
                from += f;
                to   += t;
            }
            mappings.put(from, to);
        }

        new Rot13Solver().solve(mappings);
    }
}
