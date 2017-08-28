package genecode.function.test;

import genecode.function.*;
import genecode.function.BinaryLogic.And;
import genecode.function.BinaryLogic.Or;
import genecode.function.BinaryLogic.Xor;
import genecode.function.Compare.LT;
import genecode.function.Compare.LTE;
import genecode.function.Compare.EQ;
import genecode.function.Compare.GTE;
import genecode.function.Compare.GT;


import java.util.Arrays;

import junit.framework.TestCase;

/**
 * Test the various functions.
 */
public class FunctionTest
    extends TestCase
{
    /**
     * Test simple arithmetic.
     */
    public void testArithmetic()
    {
        for (int i1 = -5; i1 <= 5; i1++) {
            for (int i2 = -5; i2 <= 5; i2++) {
                final Double d1 = Double.valueOf(i1);
                final Double d2 = Double.valueOf(i2);
                assertEquals(Double.valueOf(d1 + d2), new Add (Double.class).call(d1, d2));
                assertEquals(Double.valueOf(d1 - d2), new Sub (Double.class).call(d1, d2));
                assertEquals(Double.valueOf(d1 * d2), new Mult(Double.class).call(d1, d2));
                assertEquals(Double.valueOf(d1 / d2), new Div (Double.class).call(d1, d2));
            }
        }
    }

    /**
     * Test logical operations.
     */
    public void testLogic()
    {
        final Boolean[] bools = { Boolean.valueOf(true),
                                  Boolean.valueOf(false) };
        for (Boolean b1 : bools) {
            assertEquals(Boolean.valueOf(!b1), new Not().call(b1));
            for (Boolean b2 : bools) {
                assertEquals(Boolean.valueOf(b1 && b2), new And().call(b1, b2));
                assertEquals(Boolean.valueOf(b1 || b2), new Or ().call(b1, b2));
                assertEquals(Boolean.valueOf(b1  ^ b2), new Xor().call(b1, b2));
            }
        }
    }

    /**
     * Test string operations.
     */
    public void testStrings()
    {
        final String[] strings = { "a", "b,b", "c,c,c" };
        String concatWant = "";
        Object concatGot = "";
        for (String string : strings) {
            final String[] array = (String[])new StringSplit().call(string, ",");
            assertTrue(
                "split(" + string + ", \",\") -> " + Arrays.toString(array),
                Arrays.equals(string.split(","), array)
            );

            concatWant += string;
            concatGot = new StringConcat().call(concatGot, string);
        }

        assertEquals(concatWant, concatGot);

        // Test splittng with an empty string
        final String hello = "Hello";
        final String[] array1 = (String[])new StringSplit().call(hello, "");
        assertEquals("Bad null split: " + Arrays.toString(array1),
                     hello.length(), array1.length);
        for (int i=0; i < hello.length(); i++) {
            assertEquals(Character.toString(hello.charAt(i)), array1[i]);
        }

        // Test splittng with a 2char string
        final String[] array2 = (String[])new StringSplit().call(hello, "ll");
        assertEquals("Bad null split: " + Arrays.toString(array2),
                     2, array2.length);
        assertEquals("He", array2[0]);
        assertEquals("o",  array2[1]);

        // Test splitting with the same string
        final String[] array3 = (String[])new StringSplit().call(hello, hello);
        assertEquals("Bad same-string split: " + Arrays.toString(array3),
                     1, array3.length);
        assertEquals("", array3[0]);
        

        // Test substring
        final String string = "Hello World";
        final int    length = string.length();
        final Substring substring = new Substring();
        for (int i = -length; i < length; i++) {
            final int from = (i < 0) ? length + i : i; 
            for (int j = -length; j < length; j++) {
                final int to = (j < 0) ? length + j : j;
                final String want =
                    (to < from) ? "" : string.substring(from, to);
                final String got =
                    (String)substring.call(string, Long.valueOf(i), Long.valueOf(j));
                assertEquals("\"" + string + "\".substring(" + from + ", " + to + ") != " +
                             "substring(\"" + string + "\", " + i + ", " + j + ")",
                             want, got);
            }
        }

        // Test joining
        final Join join = new Join();
        for (int i=0; i < 10; i++) {
            final String[] joinees = new String[i];
            for (int j=0; j < i; j++) {
                joinees[j] = Integer.toString(j);
            }

            final String joined = (String)join.call(joinees, ":");

            assertEquals((i == 0) ? 0 : 2 * i - 1, joined.length());
            for (int j=0; j < i; j++) {
                assertEquals(joinees[j].charAt(0), joined.charAt(2 * j));
                if (j + 1 < i) {
                    assertEquals(':', joined.charAt(2 * j + 1));
                }
            }
        }

        // Test replacing
        final Replace replace = new Replace(String.class);
        /*scope*/ {
            final String str = "Hello World";
            assertEquals("Hello World", replace.call(str, "l",  "k",   0));
            assertEquals("Heklo World", replace.call(str, "l",  "k",   1));
            assertEquals("Hekko World", replace.call(str, "l",  "k",   2));
            assertEquals("Hekko Workd", replace.call(str, "l",  "k",  10));
            assertEquals("Hesho World", replace.call(str, "ll", "sh", 10));
            assertEquals("ello World",  replace.call(str, "H",  "",   10));
            assertEquals("Hello Worl",  replace.call(str, "d",  "",   10));
        }
    }

    /**
     * Test array operations.
     */
    public void testArrays()
    {
        final Double[] values = new Double[10];
        Double value  = Double.valueOf(0);
        for (int i=0; i < values.length; i++) {
            values[i] = Double.valueOf(i);
            value = value + Double.valueOf(i);
        }

        final Map map = new Map(new Add(Double.class));
        Double[] array = (Double[])map.call(values, values);
        assertTrue(array != null);
        assertEquals(values.length, array.length);
        for (int i=0; i < array.length; i++) {
            assertTrue("Element #" + i + " was null",
                       array[i] != null);
            assertEquals(values[i] * 2, array[i].doubleValue());
        }

        final Reduce reduce = new Reduce(new Add(Double.class));
        Double dble = (Double)reduce.call((Object)values);
        assertEquals(value, dble);

        final Reverse reverse = new Reverse(values.getClass());
        final Double[] reversed = (Double[])reverse.call((Object)values);
        assertEquals(values.length, reversed.length);
        for (int i=0; i < values.length; i++) {
            assertEquals(values[values.length - 1 - i], reversed[i]);
        }

        final RemoveAt removeAt = new RemoveAt(values.getClass(), Long.class);
        for (int i=0; i < values.length; i++) {
            final Double[] removed =
                (Double[])removeAt.call(values, Long.valueOf(i));
            assertEquals(values.length - 1, removed.length);
            for (int j=0, k=0; j < values.length; j++) {
                if (i != j) {
                    assertEquals(values[j], removed[k++]);
                }
            }
        }

        final InsertAt insertAt = new InsertAt(values.getClass(), Long.class);
        for (int i=0; i <= values.length; i++) {
            final Double insert = -1.0;
            final Double[] inserted =
                (Double[])insertAt.call(values, Long.valueOf(i), insert);
            assertEquals(values.length + 1, inserted.length);
            for (int j=0, k=0; j < inserted.length; j++) {
                if (i != j) {
                    assertEquals(values[k++], inserted[j]);
                }
                else {
                    assertEquals(insert, inserted[j]);
                }
            }
        }

        for (int i=0; i <= 10; i++) {
            final ArrayOf arrayOf = new ArrayOf(Integer.class, i);
            final Object[] args = new Object[i];
            for (int j=0; j < i; j++) {
                args[j] = Integer.valueOf(j);
            }

            final Integer[] wrapped = (Integer[])arrayOf.call(args);

            assertEquals(Arrays.toString(wrapped) + " != " + Arrays.toString(args),
                         args.length, wrapped.length);
            for (int j=0; j < i; j++) {
                assertEquals(args[j], wrapped[j]);
            }
        }

        final Concat concat = new Concat(new Integer[0].getClass());
        for (int i=0; i <= 10; i++) {
            final Integer[] ints = new Integer[i];
            for (int j=0; j < i; j++) {
                ints[j] = Integer.valueOf(j);
            }

            final Integer[] joint = (Integer[])concat.call(ints, ints);

            assertEquals(Arrays.toString(joint) + " != " + Arrays.toString(ints),
                         ints.length * 2, joint.length);
            for (int j=0; j < 2 * i; j++) {
                assertEquals(ints[j % i], joint[j]);
            }
        }

        /*scope*/ {
            final Replace replace = new Replace(new Integer[0].getClass());
            final Integer[] ints = new Integer[10];
            for (int j=0; j < ints.length; j++) {
                ints[j] = Integer.valueOf(j / 2);
            }

            final Integer[] none = (Integer[])replace.call(ints,
                                                           Integer.valueOf(2),
                                                           Integer.valueOf(100),
                                                           Integer.valueOf(0));
            final Integer[] one = (Integer[])replace.call(ints,
                                                          Integer.valueOf(2),
                                                          Integer.valueOf(100),
                                                          Integer.valueOf(1));
            final Integer[] all = (Integer[])replace.call(ints,
                                                          Integer.valueOf(2),
                                                          Integer.valueOf(100),
                                                          Integer.valueOf(10));
            assertEquals(ints.length, none.length);
            assertEquals(ints.length, one .length);
            assertEquals(ints.length, all .length);
            for (int j=0; j < ints.length; j++) {
                assertEquals(ints[j], none[j]);
                if (j == 4) {
                    assertEquals(Integer.valueOf(100), one[j]);
                    assertEquals(Integer.valueOf(100), all[j]);
                }
                else if (j == 5) {
                    assertEquals(ints[j],              one[j]);
                    assertEquals(Integer.valueOf(100), all[j]);
                }
                else {
                    assertEquals(ints[j], one[j]);
                    assertEquals(ints[j], all[j]);
                }
            }
        }
        
    }
}
