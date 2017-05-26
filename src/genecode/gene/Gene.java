package genecode.gene;

import genecode.Context;
import genecode.Genome;

import java.io.Serializable;

import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import java.util.logging.Logger;

/**
 * A gene in the genome.
 *
 * <p>It is important that Gene instances are not shared between
 * {@link Genome} instances. As such this class is {@link Cloneable}.
 *
 * <p>Nothing, except the {@link Genome}, should hold a direct
 * reference to any instance of this class, instead one should look up
 * instances via the {@link Handle} instead.
 *
 * <p>Generally speaking, all implementations should be stateless and
 * only require a {@link Context} instance for evaluation.
 */
public interface Gene
    extends Cloneable,
            Serializable
{
    /**
     * A handle on a gene.
     *
     * <p>This is essentially an anonymous pointer. However, we use
     * these so that one Gene may reference another without holding on
     * to a direct pointer.
     *
     * <p>Note that this is unique to a gene and <i>copies</i> of that
     * gene but <i>not</i> unique to the class of that gene. I.e. two
     * {@code FooGene} instances may have different handles but a copy
     * of one of those instances will share the handle of the gene
     * which it was copied from.
     */
    public static final class Handle
        implements Serializable
    {
        private static final long serialVersionUID = 782829319877849781L;

        /**
         * Our next handle ID.
         */
        private static final AtomicLong ourNextId = new AtomicLong();

        /**
         * Our ID.
         */
        private final long myId = ourNextId.getAndIncrement();

        /**
         * Get the {@link Gene} referenced by this handle in the given
         * {@link Genome}, if any.
         *
         * @param genome The genome to get the gene from for this handle.
         *
         * @return The {@link Gene} instance, or {@code null} if no
         * instance was found in the given {@link Genome}.
         */
        public Gene get(final Genome genome)
        {
            return (genome == null) ? null : genome.get(this);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public int hashCode()
        {
            return Long.hashCode(myId);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public boolean equals(final Object that)
        {
            return (that instanceof Handle) && ((Handle)that).myId == myId;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public String toString()
        {
            return "Handle[" + myId + "]";
        }
    }

    // ----------------------------------------------------------------------

    /**
     * Our logger.
     */
    public static final Logger LOG = Logger.getLogger(Gene.class.getName());

    // ----------------------------------------------------------------------

    /**
     * Initialise this gene using the genome that it's in.
     *
     * @param genome  The genome which this gene will live inside.
     *
     * @throws IllegalStateException If this is called more than once.
     */
    public void init(final Genome genome)
        throws IllegalStateException;

    /**
     * The {@link Handle} associated with this gene.
     *
     * @return The handle of this gene.
     */
    public Handle getHandle();

    /**
     * Get the size of the evaluation graph represented by gene,
     * within the given genome.
     *
     * @param genome The genome which this gene lives in.
     *
     * @return The graph size.
     */
    public int getGraphSize(final Genome genome);

    /**
     * Get the list of handles in the graph.
     *
     * @param genome The genome to use for determining representations.
     * @param dest   The list to accumulate into.
     */
    public void getGraphHandles(final Genome            genome,
                                final List<Gene.Handle> dest);

    /**
     * Get the return type of this gene.
     *
     * @return The type of the return value.
     */
    public Class<?> getReturnType();

    /**
     * Evaluate the gene.
     *
     * @param context The context in which we are mutating.
     * @param genome  The genome of which we are a part.
     *
     * @return {@code null} if the gene could not be evaluated.
     *
     * @throws IllegalStateException If this is called before {@code init()}.
     */
    public Object evaluate(final Context context,
                           final Genome  genome)
        throws IllegalStateException;

    /**
     * Mutate the gene.
     *
     * @param genome  The genome which this gene lives inside.
     * @param factor  The mutation factor, between 0 and 1.
     */
    public void mutate(final Genome genome,
                       final double factor);

    /**
     * Get the string representation of this gene, using the given
     * genome for reference.
     *
     * @param genome The genome to use for determining representations.
     *
     * @return The textual representation of this gene.
     */
    public String toString(final Genome genome);

    /**
     * Create a duplicate of this instance.
     *
     * @return The duplicate.
     */
    public Gene clone();
}
