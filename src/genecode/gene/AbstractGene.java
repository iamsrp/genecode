package genecode.gene;

import genecode.Context;
import genecode.Genome;

import java.io.Serializable;

import java.util.List;
import java.util.Objects;
import java.util.logging.Level;

/**
 * The base class for Genes which provides some handy mechanics.
 */
public abstract class AbstractGene
    implements Gene,
               Serializable               
{
    /**
     * The type that we return.
     */
    private final Class<?> myReturnType;

    /**
     * Our handle.
     */
    private final Handle myHandle;

    /**
     * Whether this gene has been init()'d.
     */
    private boolean myInitted;

    /**
     * Whether this gene is currently being evaluated.
     */
    private boolean myIsEvaluating;

    // Used the cache the result for the last evaluation
    private long   myCachedContextId;
    private long   myCachedGenomeId;
    private Object myCachedValue;

    // ----------------------------------------------------------------------

    /**
     * CTOR.
     *
     * @param returnType The type of the value which we return.
     */
    protected AbstractGene(final Class<?> returnType)
    {
        Objects.requireNonNull(returnType);
        
        myReturnType      = returnType;
        myHandle          = new Handle();
        myInitted         = false;
        myIsEvaluating    = false;
        myCachedContextId = -1;
        myCachedGenomeId  = -1;
        myCachedValue     = null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void init(final Genome genome)
        throws IllegalStateException
    {
        if (myInitted) {
            throw new IllegalStateException("Already init()'d " + this);
        }
        else {
            safeInit(genome);
            myInitted = true;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Handle getHandle()
    {
        return myHandle;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getGraphSize(final Genome genome)
    {
        return 1; // Us
    }

    /**
     * {@inheritDoc}
     */
    @Override    
    public void getGraphHandles(final Genome            genome,
                                final List<Gene.Handle> dest)
    {
        dest.add(getHandle());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Class<?> getReturnType()
    {
        return myReturnType;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final Object evaluate(final Context context,
                                 final Genome  genome)
        throws IllegalStateException
    {
        // Make sure we're ready
        if (!myInitted) {
            throw new IllegalStateException(this + " not init()'d");
        }

        // Ensure we've not entered a circular dependency
        if (myIsEvaluating) {
            return null;
        }

        // See if we have a cached value
        if (context.getId() == myCachedContextId &&
            genome .getId() == myCachedGenomeId)
        {
            return myCachedValue;
        }

        // Okay, we're evaluating
        myIsEvaluating = true;
        try {
            final Object result = safeEvaluate(context, genome);
            if (LOG.isLoggable(Level.FINEST)) {
                LOG.finest(
                    getClass().getSimpleName() + '{' + toString(genome) + "} => " +
                    result
                );
            }

            // Cache it
            myCachedContextId = context.getId();
            myCachedGenomeId  = genome .getId();
            myCachedValue     = result;

            // And give it back
            return result;
        }
        catch (Exception e) {
            // Don't expect this
            LOG.log(Level.SEVERE, "Failed to evaluate: " + toString(genome), e);
            return null;
        }
        finally {
            myIsEvaluating = false;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString()
    {
        return getClass().getSimpleName();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(final Object o)
    {
        if (!(o instanceof AbstractGene)) {
            return false;
        }

        if (!o.getClass().equals(getClass())) {
            return false;
        }

        final AbstractGene that = (AbstractGene)o;
        if (!that.myHandle.equals(myHandle)) {
            return false;
        }

        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Gene clone()
    {
        try {
            final AbstractGene result = (AbstractGene)super.clone();
            result.myIsEvaluating    = false;
            result.myCachedContextId = -1;
            result.myCachedGenomeId  = -1;
            result.myCachedValue     = null;
            return result;
        }
        catch (CloneNotSupportedException e) {
            // Should not happen
            throw new RuntimeException(e);
        }
    }

    // ----------------------------------------------------------------------

    /**
     * Flush the caches.
     */
    protected void flush()
    {
        myCachedContextId = -1;
        myCachedGenomeId  = -1;
        myCachedValue     = null;
    }

    /**
     * Actually init the gene. Only called when we know it's okay to
     * do so.
     *
     * @param genome  The genome which this gene will live inside.
     *
     * @throws IllegalStateException If there was a problem with the init.
     */
    protected abstract void safeInit(final Genome genome)
        throws IllegalStateException;

    /**
     * Actually evaluate the value. Only called when we know it's okay
     * to do so.
     *
     * @param context The context to evaluate this gene in.
     * @param genome  The genome which this gene lives inside.
     *
     * @return The result of the evaluation.
     */
    protected abstract Object safeEvaluate(final Context context,
                                           final Genome  genome);
}
