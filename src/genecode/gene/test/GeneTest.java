package genecode.gene.test;

import genecode.Context;
import genecode.Context.Identifier;
import genecode.Genome;
import genecode.gene.*;
import genecode.function.Add;
import genecode.function.BinaryLogic.Xor;
import genecode.function.StringConcat;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import java.util.Arrays;

import junit.framework.TestCase;

/**
 * Test the various genes.
 */
public class GeneTest
    extends TestCase
{
    /**
     * Our context.
     */
    private final Context myContext;

    /**
     * Our genome.
     */
    private final Genome myGenome;

    // Identifiers
    private final Identifier<Double> myIdentifier;

    // The genes
    private final Accessor myAccessor;

    private final FunctionGene myAdd;
    private final FunctionGene myStringConcat;
    private final FunctionGene myXor;

    private final ConstantBoolean myBooleanTrue;
    private final ConstantBoolean myBooleanFalse;

    private final ConstantDouble myDoubleFive;
    private final ConstantDouble myDoubleTwo;
    private final ConstantDouble myDoubleOne;
    private final ConstantDouble myDoubleZero;
    private final ConstantDouble myDoubleMinusOne;
    private final ConstantDouble myDoubleMinusTwo;
    private final ConstantDouble myDoubleMinusFive;
    private final ConstantDouble myDoubleNaN;

    private final ConstantString myStringHello;
    private final ConstantString myStringWorld;

    private final MemoryGene myMemoryGene;

    /**
     * CTOR.
     */
    public GeneTest()
    {
        myContext =
            new Context()
            {
                @Override
                public long getId()
                {
                    return -1L;
                }

                @Override
                public Object access(final Identifier id)
                {
                    if (myIdentifier.equals(id)) {
                        return Double.valueOf(-12345.0);
                    }
                    else {
                        return super.access(id);
                    }
                }
            };

        myIdentifier = new Identifier<Double>("id", Double.class);

        // All the gene classes
        myAccessor        = new Accessor<Double>(myIdentifier, Double.class);
        myAdd             = new FunctionGene(new Add(Double.class));
        myStringConcat    = new FunctionGene(new StringConcat());
        myXor             = new FunctionGene(new Xor());

        myBooleanTrue     = new ConstantBoolean(true);
        myBooleanFalse    = new ConstantBoolean(false);

        myDoubleFive      = new ConstantDouble( 5);
        myDoubleTwo       = new ConstantDouble( 2);
        myDoubleOne       = new ConstantDouble( 1);
        myDoubleZero      = new ConstantDouble( 0);
        myDoubleMinusOne  = new ConstantDouble(-1);
        myDoubleMinusTwo  = new ConstantDouble(-2);
        myDoubleMinusFive = new ConstantDouble(-5);
        myDoubleNaN       = new ConstantDouble(Double.NaN);

        myStringHello     = new ConstantString("Hello");
        myStringWorld     = new ConstantString("World");

        myMemoryGene      = new MemoryGene(Double.class);

        // And create the genome
        myGenome =
            new Genome(
                () -> null, // Unused
                Arrays.asList(
                    Double.class
                ),
                Arrays.asList(
                    myAccessor,
                    myAdd,
                    myStringConcat,
                    myXor,

                    myBooleanTrue,
                    myBooleanFalse,

                    myDoubleFive,
                    myDoubleTwo,
                    myDoubleOne,
                    myDoubleZero,
                    myDoubleMinusOne,
                    myDoubleMinusTwo,
                    myDoubleMinusFive,
                    myDoubleNaN,

                    myStringHello,
                    myStringWorld,

                    myMemoryGene
                )
            );
    }

    // ----------------------------------------------------------------------

    /**
     * Test constants.
     */
    public void testConstants()
    {
        assertEquals(myDoubleMinusFive.evaluate(myContext, myGenome), -5.0);
        assertEquals(myDoubleMinusTwo .evaluate(myContext, myGenome), -2.0);
        assertEquals(myDoubleMinusOne .evaluate(myContext, myGenome), -1.0);
        assertEquals(myDoubleZero     .evaluate(myContext, myGenome),  0.0);
        assertEquals(myDoubleOne      .evaluate(myContext, myGenome),  1.0);
        assertEquals(myDoubleTwo      .evaluate(myContext, myGenome),  2.0);
        assertEquals(myDoubleFive     .evaluate(myContext, myGenome),  5.0);

        assertTrue(Double.isNaN((Double)myDoubleNaN.evaluate(myContext, myGenome)));
    }

    /**
     * Test accessors.
     */
    public void testAccessor()
    {
        assertEquals(myAccessor.evaluate(myContext, myGenome), -12345.0);
    }

    /**
     * Test simple arithmetic.
     */
    public void testArithmetic()
    {
        myAdd.setArgs(myDoubleFive    .getHandle(),
                      myDoubleMinusTwo.getHandle());
        assertEquals(myAdd                   .evaluate(myContext, myGenome),
                     (Double)myDoubleFive    .evaluate(myContext, myGenome) +
                     (Double)myDoubleMinusTwo.evaluate(myContext, myGenome));
    }

    /**
     * Test strings.
     */
    public void testString()
    {
        myStringConcat.setArgs(myStringHello.getHandle(),
                               myStringWorld.getHandle());
        assertEquals(myStringConcat       .evaluate(myContext, myGenome),
                     (String)myStringHello.evaluate(myContext, myGenome) +
                     (String)myStringWorld.evaluate(myContext, myGenome));
    }

    /**
     * Test logical operations.
     */
    public void testLogic()
    {
        myXor.setArgs(myBooleanTrue.getHandle(),
                      myBooleanTrue.getHandle());
        assertEquals(myXor         .evaluate(myContext, myGenome),
                     myBooleanFalse.evaluate(myContext, myGenome));

        myXor.setArgs(myBooleanTrue .getHandle(),
                      myBooleanFalse.getHandle());
        assertEquals(myXor        .evaluate(myContext, myGenome),
                     myBooleanTrue.evaluate(myContext, myGenome));

        myXor.setArgs(myBooleanFalse.getHandle(),
                      myBooleanTrue .getHandle());
        assertEquals(myXor        .evaluate(myContext, myGenome),
                     myBooleanTrue.evaluate(myContext, myGenome));

        myXor.setArgs(myBooleanFalse.getHandle(),
                      myBooleanFalse.getHandle());
        assertEquals(myXor         .evaluate(myContext, myGenome),
                     myBooleanFalse.evaluate(myContext, myGenome));
    }

    /**
     * Test memory.
     */
    public void testMemory()
    {
        // Two different context with different IDs so that we force
        // the delay
        final Context context0 =
            new Context() {
                @Override public long getId() { return 0; }
                @Override public Object access(Identifier<?> id) { return null; }
            };
        final Context context1 =
            new Context() {
                @Override public long getId() { return 1; }
                @Override public Object access(Identifier<?> id) { return null; }
            };

        // First should be null
        myMemoryGene.setSource(myGenome, myDoubleTwo.getHandle());
        assertEquals(myMemoryGene.evaluate(context0, myGenome),
                     null);

        // Now we should see the 2 from above
        myMemoryGene.setSource(myGenome, myDoubleFive.getHandle());
        assertEquals(myMemoryGene.evaluate(context1, myGenome),
                     myDoubleTwo .evaluate(context0, myGenome));

        // And now the five
        myMemoryGene.setSource(myGenome, myDoubleOne.getHandle());
        assertEquals(myMemoryGene.evaluate(context0, myGenome),
                     myDoubleFive.evaluate(context1, myGenome));
    }

    /**
     * Test marshalling.
     *
     * @throws ClassNotFoundException From unmarshalling.
     * @throws IOException            From (un)marshalling.
     */
    public void testIO()
        throws ClassNotFoundException,
               IOException
    {
        for (int i=0; i < myGenome.getGenomeSize(); i++) {
            final Gene out = myGenome.get(i);

            // Make sure equals() works in the first place
            assertEquals(out, out);

            // Now marshall out/in and compare
            try {
                final ByteArrayOutputStream baos = new ByteArrayOutputStream();
                final ObjectOutputStream os = new ObjectOutputStream(baos);
            
                os.writeObject(out);

                final ObjectInputStream is =
                    new ObjectInputStream(new ByteArrayInputStream(baos.toByteArray()));
                final Gene in = (Gene)is.readObject();

                assertEquals(out, in);
            }
            catch (IOException e) {
                throw new IOException("Failed to marshall " + out, e);                   
            }
        }
    }
}
