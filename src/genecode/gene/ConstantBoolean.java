package genecode.gene;

import genecode.Context;
import genecode.Genome;

/**
 * A gene which is always a constant boolean value.
 */
public class ConstantBoolean
    extends AbstractGene
{
    /**
     * The value which we hold.
     */
    private final Boolean myValue;

    /**
     * CTOR.
     *
     * @param value The value of this constant.
     */
    public ConstantBoolean(final boolean value)
    {
        super(Boolean.class);
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
        return Boolean.toString(myValue);
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
    protected final Boolean safeEvaluate(final Context context,
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
        return super.toString() + '[' + myValue + ']';
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

        if (!(o instanceof ConstantBoolean)) {
            return false;
        }

        final ConstantBoolean that = (ConstantBoolean)o;
        return that.myValue.equals(myValue);
    }
}
