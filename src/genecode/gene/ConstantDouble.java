package genecode.gene;

import genecode.Context;
import genecode.Genome;

/**
 * A gene which is always a constant double value.
 */
public class ConstantDouble
    extends AbstractGene
{
    /**
     * The value which we hold.
     */
    private final Double myValue;

    /**
     * CTOR.
     *
     * @param value The value of this constant.
     */
    public ConstantDouble(final double value)
    {
        super(Double.class);
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
        return Double.toString(myValue);
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
    protected final Double safeEvaluate(final Context context,
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

        if (!(o instanceof ConstantDouble)) {
            return false;
        }

        final ConstantDouble that = (ConstantDouble)o;

        if (Double.isNaN(that.myValue) && Double.isNaN(myValue)) {
            return true;
        }
        else {
            return that.myValue.equals(myValue);
        }
    }
}
