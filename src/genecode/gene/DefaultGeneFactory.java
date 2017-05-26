package genecode.gene;

import genecode.ArrayUtil;;

import genecode.function.Add;
import genecode.function.ArrayOf;
import genecode.function.BinaryLogic.And;
import genecode.function.BinaryLogic.Or;
import genecode.function.BinaryLogic.Xor;
import genecode.function.Compare.EQ;
import genecode.function.Compare.GT;
import genecode.function.Compare.GTE;
import genecode.function.Compare.LT;
import genecode.function.Compare.LTE;
import genecode.function.Concat;
import genecode.function.Div;
import genecode.function.Exp;
import genecode.function.Function;
import genecode.function.GetAt;
import genecode.function.IfElse;
import genecode.function.InsertAt;
import genecode.function.Length;
import genecode.function.Log;
import genecode.function.Map;
import genecode.function.Mod;
import genecode.function.Mult;
import genecode.function.Neg;
import genecode.function.Not;
import genecode.function.NumberCast;
import genecode.function.ParseNumber;
import genecode.function.Range;
import genecode.function.Reduce;
import genecode.function.RemoveAt;
import genecode.function.Repeat;
import genecode.function.Reverse;
import genecode.function.Select;
import genecode.function.StringConcat;
import genecode.function.StringSplit;
import genecode.function.Substring;
import genecode.function.Sub;
import genecode.function.ToLower;
import genecode.function.ToUpper;

import java.lang.reflect.Array;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;

/**
 * The default implementation of {@link GeneFactory}.
 */
public class DefaultGeneFactory
    implements GeneFactory
{
    /**
     * Get the Collection of default suppliers.
     */
    @SuppressWarnings("unchecked")
    /*package*/ static Collection<Supplier<Gene>> getSuppliers()
    {
        final List<Class<? extends Number>> numberClasses = new ArrayList<>();
        numberClasses.add(Long  .class);
        numberClasses.add(Double.class);

        final List<Class<?>> classes = new ArrayList<>();
        classes.addAll(numberClasses);
        classes.add   (String.class);

        final int[] primes = { 1, 2, 3, 5, 7, 11 };

        // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -

        final List<Supplier<Gene>> suppliers = new ArrayList<>();

        // Constants
        suppliers.add(() -> new ConstantBoolean(true));
        suppliers.add(() -> new ConstantBoolean(false));

        suppliers.add(() -> new ConstantDouble(0));
        suppliers.add(() -> new ConstantLong  (0));
        for (int i : primes) {
            suppliers.add(() -> new ConstantDouble(-i));
            suppliers.add(() -> new ConstantDouble( i));
            suppliers.add(() -> new ConstantLong  (-i));
            suppliers.add(() -> new ConstantLong  ( i));
        }

        suppliers.add(() -> new ConstantString(""));
        for (int b=0; b < 128; b++) {
            final char c = (char)b;
            suppliers.add(() -> new ConstantString(Character.toString(c)));
        }

        // Values
        suppliers.add(() -> new DoubleValue());
        suppliers.add(() -> new LongValue  ());

        // All the functions that we know about
        final List<Function> functions = new ArrayList<>();

        // Binary operators
        functions.add(new And());
        functions.add(new Or ());
        functions.add(new Xor());
        functions.add(new Not());

        // String operators
        functions.add(new Length      (String.class));
        functions.add(new StringConcat());
        functions.add(new StringSplit ());
        functions.add(new Substring   ());
        functions.add(new ToUpper     ());
        functions.add(new ToLower     ());

        // Conditional logic
        for (Class<?> klass : classes) {
            functions.add(new IfElse(klass));
        }

        // Numeric operators
        for (Class<? extends Number> klass : numberClasses) {
            functions.add(new Add (klass));
            functions.add(new Div (klass));
            functions.add(new Mod (klass));
            functions.add(new Mult(klass));
            functions.add(new Neg (klass));
            functions.add(new Sub (klass));
            for (Class<? extends Number> klass2 : numberClasses) {
                if (klass != klass2) {
                    functions.add(new NumberCast(klass, klass2));
                }
            }

            functions.add(new ParseNumber(klass));
            functions.add(new Range      (klass));
        }
        functions.add(new Exp(Double.class));
        functions.add(new Log(Double.class));

        // Collection operations
        for (Class<?> klass : classes) {
            // Repeat a few times to handle multi-dimensional arrays
            for (int i=0; i < 2; i++) {
                final Class<?> arrayClass = ArrayUtil.arrayType(klass);

                // Add the functions
                functions.add(new GetAt   (arrayClass, Long.class));
                functions.add(new InsertAt(arrayClass, Long.class));
                functions.add(new RemoveAt(arrayClass, Long.class));
                functions.add(new Length  (arrayClass));
                functions.add(new Reverse (arrayClass));
                functions.add(new Concat  (arrayClass));
                functions.add(new Select  (arrayClass));

                for (int j=0; j < 5; j++) {
                    functions.add(new ArrayOf(klass, j));
                }
                functions.add(new Repeat(klass, 32)); // arbitrary cap

                // Roll to the next dimension
                klass = arrayClass;
            }
        }

        // Other operations
        for (Class<?> klass : classes) {
            if (Comparable.class.isAssignableFrom(klass)) {
                functions.add(new LT ((Class<Comparable<?>>)klass));
                functions.add(new LTE((Class<Comparable<?>>)klass));
                functions.add(new EQ ((Class<Comparable<?>>)klass));
                functions.add(new GTE((Class<Comparable<?>>)klass));
                functions.add(new GT ((Class<Comparable<?>>)klass));
            }
        }

        // Add any functions which we can to Map and Reduce
        for (int i = 0, size = functions.size(); i < size; i++) {
            final Function function = functions.get(i);
            if (Reduce.isApplicable(function)) {
                functions.add(new Map(function));
            }
            if (Reduce.isApplicable(function)) {
                functions.add(new Reduce(function));
            }
        }

        // Now add the genes with the functions in them
        for (Function function : functions) {
            suppliers.add(() -> new FunctionGene(function));
        }

        // And give the genes back
        return Collections.unmodifiableList(suppliers);
    }

    /**
     * The list of gene creators which we know about.
     */
    private final List<Supplier<Gene>> myGenerators;

    /**
     * CTOR.
     */
    public DefaultGeneFactory()
    {
        myGenerators = new ArrayList<>(getSuppliers());
    }

    /**
     * CTOR with more suppliers.
     *
     * @param suppliers Additional {@link Supplier}s for creating genes.
     */
    @SuppressWarnings("unchecked")
    public DefaultGeneFactory(Supplier<Gene>... suppliers)
    {
        this();
        for (Supplier<Gene> supplier : suppliers) {
            myGenerators.add(supplier);
        }
    }

    /**
     * CTOR with more suppliers.
     *
     * @param suppliers Additional {@link Supplier}s for creating genes.
     */
    public DefaultGeneFactory(Collection<Supplier<Gene>> suppliers)
    {
        this();
        for (Supplier<Gene> supplier : suppliers) {
            myGenerators.add(supplier);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Gene generate()
    {
        final Supplier<Gene> generator =
            myGenerators.get((int)(myGenerators.size() * Math.random()));
        return generator.get();
    }
}
