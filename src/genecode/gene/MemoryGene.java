package genecode.gene;

import genecode.Context;
import genecode.Genome;

import java.util.Objects;

/**
 * A gene which remembers things from another gene.
 *
 * <p>This gene will persist a value for a single context instance,
 * giving back the value from the previous context instance when
 * evaluated. Chaining instances will effectively create a delay
 * buffer.
 */
public class MemoryGene
    extends AbstractGene
{
    /**
     * The gene from which we copy values.
     */
    private Gene.Handle mySource;

    /**
     * Cached gene instance.
     */
    private Gene mySourceGene;

    /**
     * The genome from which the cached gene was retrieved.
     */
    private Genome mySourceGenome;

    /**
     * The value which we have remembered.
     */
    private Object myValue;

    /**
     * CTOR.
     *
     * @param returnType The type which we will hand back.
     */
    public MemoryGene(final Class<?> returnType)
    {
        super(returnType);
        mySource       = null;
        mySourceGene   = null;
        mySourceGenome = null;
        myValue        = null;
    }

    /**
     * Set the source gene. Mainly for testing.
     */
    public void setSource(final Genome      genome,
                          final Gene.Handle source)
    {
        mySource       = source;
        mySourceGene   = genome.get(source);
        mySourceGenome = genome;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getGraphSize(final Genome genome)
    {
        // Us, plus whatever we pull from
        final Gene gene = getGene(genome);
        return 1 + (gene == null ? 0 : gene.getGraphSize(genome));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final void mutate(final Genome genome,
                             final double factor)
    {
        if (Math.random() < factor) {
            mySource = genome.pickAnyHandle(getReturnType());
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final String toString(final Genome genome)
    {
        final Gene gene = getGene(genome);
        return "MemoryGene[" +
            (gene == null ? "null" : gene.toString(genome)) + "->" + myValue +
        ']';
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Gene clone()
    {
        final MemoryGene gene = (MemoryGene)super.clone();
        // mySource is safe to copy
        gene.mySourceGene   = null;
        gene.mySourceGenome = null;
        gene.myValue        = null;
        return gene;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void flush()
    {
        super.flush();
        mySourceGene   = null;
        mySourceGenome = null;
        myValue        = null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected final void safeInit(final Genome genome)
    {
        mySource       = genome.pickAnyHandle(getReturnType());
        mySourceGene   = null;
        mySourceGenome = null;
        myValue        = null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected final Object safeEvaluate(final Context context,
                                        final Genome  genome)
    {
        // This method relies on the AbstractGene's caching logic to
        // ensure that it is only called for a new context instance
        // and, as such, only pops the remembered value once per
        // context. We could duplicate that logic here for safety but
        // there seems little point (famous last words).

        // What we'll hand back; pulled from last time we saved the
        // value
        final Object result = myValue;

        // Get the new value to remember
        final Gene gene = getGene(genome);
        myValue = (gene == null) ? null : gene.evaluate(context, genome);

        // And give back the old value
        return result;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final String toString()
    {
        return super.toString() + '[' + mySource + "->" + myValue + ']';
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

        if (!(o instanceof MemoryGene)) {
            return false;
        }

        final MemoryGene that = (MemoryGene)o;
        return Objects.equals(this.mySource, that.mySource);
    }

    /**
     * Get the gene from the genome,
     */
    private Gene getGene(final Genome genome)
    {
        // See if the cached version is still valid
        if (genome != mySourceGenome) { // <- Pointer compare
            // Look for it again
            mySourceGene   = genome.get(mySource);
            mySourceGenome = genome;
        }

        // Safe to hand back
        return mySourceGene;
    }
}
