package genecode.gene;

import genecode.Context;
import genecode.Genome;

/**
 * A gene which is just a simple long value.
 */
public class LongValue
    extends AbstractGene
{
    private static final long serialVersionUID = 78863467235567L;

    /**
     * Our value.
     */
    private Long myValue;

    /**
     * CTOR.
     */
    public LongValue()
    {
        super(Long.class);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void mutate(final Genome genome,
                       final double factor)
    {
        myValue += (long)(factor * 20 * (Math.random() - 0.5));
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
        return Long.toString(myValue);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void safeInit(final Genome genome)
    {
        myValue = (long)(20 * (Math.random() - 0.5));
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
