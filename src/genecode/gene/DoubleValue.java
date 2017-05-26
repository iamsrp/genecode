package genecode.gene;

import genecode.Context;
import genecode.Genome;

/**
 * A gene which is just a simple double value.
 */
public class DoubleValue
    extends AbstractGene
{
    private static final long serialVersionUID = 3724876876423897L;

    /**
     * Our value.
     */
    private Double myValue;

    /**
     * CTOR.
     */
    public DoubleValue()
    {
        super(Double.class);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void mutate(final Genome genome,
                       final double factor)
    {
        myValue += factor * 2 * (Math.random() - 0.5);
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
    public String toString(final Genome genome)
    {
        return Double.toString(myValue);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void safeInit(final Genome genome)
    {
        myValue = 2 * (Math.random() - 0.5);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Object safeEvaluate(final Context context,
                                  final Genome  genome)
    {
        return myValue;
    }
}
