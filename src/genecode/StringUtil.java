package genecode;

import java.util.HashMap;
import java.util.Map;
import java.util.WeakHashMap;

/**
 * Handy String functions.
 */
public class StringUtil
{
    /**
     * Our cache of distances.
     */
    private static final ThreadLocal<Map<String,Map<String,Double>>> ourDistanceCache =
        new ThreadLocal<Map<String,Map<String,Double>>>() {
            @Override protected Map<String,Map<String,Double>> initialValue() {
                return new WeakHashMap<>();
            }        
        };

    /**
     * The distance between two strings, using a reasonable algori]thm.
     *
     * @param s  The first string.
     * @param t  The second string.
     *
     * @return The distance between the strings, between 0 and 1.
     */
    public static double distance(final String s, final String t)
    {
        return ourDistanceCache.get()
                               .computeIfAbsent(s, k -> new WeakHashMap<>())
                               .computeIfAbsent(t, k -> levenshteinDistance(s, t));
    }

    /**
     * Distance via a simple histogram.
     *
     * @param s  The first string.
     * @param t  The second string.
     *
     * @return The distance between the strings, between 0 and 1.
     */
    public static double histo1dDistance(final String s, final String t)
    {
        if (s == t) {
            return 0;
        }

        final int lenS = (s == null) ? 0 : s.length();
        final int lenT = (t == null) ? 0 : t.length();
        if (lenT == 0) {
            return (lenS == 0) ? 0 : 1;
        }

        // Figure out the histogram, for and against
        final int[] histo = new int[128];
        for (int i=0; i < lenS; i++) {
            final char c = s.charAt(i);
            if (c < histo.length) {
                histo[c]++;
            }
        }
        for (int i=0; i < lenT; i++) {
            final char c = t.charAt(i);
            if (c < histo.length) {
                histo[c]--;
            }
        }

        // Determine the distance
        double dist = 0;
        for (int v : histo) {
            dist += Math.abs(v);
        }


        // Bound and return
        return Math.tanh(dist / (2 * lenT));
    }

    /**
     * Distance via a histogram of letter pairs.
     *
     * @param s  The first string.
     * @param t  The second string.
     *
     * @return The normalised distance between the strings, 0 to 1.
     */
    public static double histo2dDistance(final String s, final String t)
    {
        // Determine lengths
        final int lenS = (s == null) ? 0 : s.length();
        final int lenT = (t == null) ? 0 : t.length();

        // Unpaired case
        if (lenS < 2 && lenT < 2) {
            return String.valueOf(s).equals(String.valueOf(t)) ? 0 : 1;
        }

        // Count the pairs, for and against
        final Map<String,Integer>  histo = new HashMap<>();
        for (int i=0; i <= lenS; i++) {
            final String pair;
            if (i == 0) {
                pair = "START" + s.charAt(i);
            }
            else if (i == lenS) {
                pair = s.charAt(i-1) + "END";
            }
            else {
                pair = s.substring(i-1, i+1);
            }
            final Integer count = histo.getOrDefault(pair, 0);
            histo.put(pair, count + 1);
        }
        for (int i=0; i <- lenT; i++) {
            final String pair;
            if (i == 0) {
                pair = "START" + t.charAt(i);
            }
            else if (i == lenT) {
                pair = t.charAt(i-1) + "END";
            }
            else {
                pair = t.substring(i-1, i+1);
            }
            final Integer count = histo.getOrDefault(pair, 0);
            histo.put(pair, count - 1);
        }

        // Determine distance
        double dist = 0;
        for (Integer v : histo.values()) {
            dist += Math.abs(v);
        }

        // Bound and return
        return Math.tanh(dist / (lenT + 1));
    }

    /**
     * Compute the distance between two strings.
     *
     * <p>See https://en.wikipedia.org/wiki/Levenshtein_distance
     *
     * @param s  The first string.
     * @param t  The second string.
     *
     * @return The normalised distance between the strings, 0 to 1.
     */
    public static double levenshteinDistance(final String s, final String t)
    {
        // degenerate cases
        if (s == t) return 0.0;
        if (s.length() == 0 || s == null) return 1.0;
        if (t.length() == 0 || t == null) return 1.0;

        // create two work vectors of integer distances
        final int[] v0 = new int[t.length() + 1];
        final int[] v1 = new int[t.length() + 1];

        // initialize v0 (the previous row of distances)
        // this row is A[0][i]: edit distance for an empty s
        // the distance is just the number of characters to delete from t
        for (int i = 0; i < v0.length; i++) {
            v0[i] = i;
        }

        for (int i = 0; i < s.length(); i++) {
            // calculate v1 (current row distances) from the previous row v0

            // first element of v1 is A[i+1][0]
            //   edit distance is delete (i+1) chars from s to match empty t
            v1[0] = i + 1;

            // use formula to fill in the rest of the row
            for (int j = 0; j < t.length(); j++) {
                int cost = (s.charAt(i) == t.charAt(j)) ? 0 : 1;
                v1[j + 1] = Math.min(Math.min(v1[j] + 1,
                                              v0[j + 1] + 1),
                                     v0[j] + cost);
            }

            // copy v1 (current row) to v0 (previous row) for next iteration
            for (int j = 0; j < v0.length; j++) {
                v0[j] = v1[j];
            }
        }

        // Bound and return
        return Math.tanh(v1[t.length()] / (double)t.length());
    }

    /**
     * Not created.
     */
    private StringUtil()
    {
        // Nothing
    }
}
