package genecode.gene;

import genecode.Context;
import genecode.Genome;

import java.util.Objects;

/**
 * A gene which is always a constant string value.
 */
public class ConstantString
    extends AbstractGene
{
    /**
     * The value which we hold.
     */
    private final String myValue;

    /**
     * CTOR.
     *
     * @param value The value of this constant.
     */
    public ConstantString(final String value)
    {
        super(String.class);
        Objects.requireNonNull(value);
        myValue = value;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getGraphSize(final Genome genome)
    {
        return 1;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final void mutate(final Genome genome,
                             final double factor)
    {
        // NOP
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final String toString(final Genome genome)
    {
        return myValue;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected final void safeInit(final Genome genome)
    {
        // NOP
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected final String safeEvaluate(final Context context,
                                        final Genome  genome)
    {
        return myValue;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final String toString()
    {
        return super.toString() + "[\"" + myValue + "\"]";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(final Object o)
    {
        if (!super.equals(o)) {
            return false;
        }

        if (!(o instanceof ConstantString)) {
            return false;
        }

        final ConstantString that = (ConstantString)o;

        return that.myValue.equals(myValue);
    }
}
