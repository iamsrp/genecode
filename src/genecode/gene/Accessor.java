package genecode.gene;

import genecode.Context;
import genecode.Context.Identifier;
import genecode.Genome;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * A gene which can access values, using {@link Identifier}s from the
 * {@link Context}.
 */
public class Accessor<T>
    extends AbstractGene
{
    private static final long serialVersionUID = 824530894750087308L;

    /**
     * The identifier.
     */
    private final Identifier<T> myIdentifier;

    /**
     * CTOR.
     *
     * @param identifier The identifier to get the value for.
     * @param returnType The type of the accessed value.
     */
    public Accessor(final Identifier<T>  identifier,
                    final Class<T>       returnType)
    {
        super(returnType);
        myIdentifier = identifier;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void mutate(final Genome genome,
                       final double factor)
    {
        // Nothing
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

        if (!(o instanceof Accessor)) {
            return false;
        }

        final Accessor that = (Accessor)o;
        if (!that.myIdentifier.equals(myIdentifier)) {
            return false;
        }

        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final String toString()
    {
        return super.toString() + '[' + myIdentifier + ']';
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final String toString(final Genome genome)
    {
        return toString();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void safeInit(final Genome genome)
    {
        // Nothing
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Object safeEvaluate(final Context context,
                                  final Genome  genome)
    {
        return context.access(myIdentifier);
    }
}
