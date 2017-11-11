package genecode;

import genecode.gene.Gene;
import genecode.gene.GeneFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicLong;

/**
 * A full genome.
 *
 * <p>Instances of this class are the canonical location for all the
 * genes in a single "organism". One gene instance may refer to
 * another via a handle; however that other gene may, or may not,
 * exist within the same genome. I.e. It's possible for one gene to
 * depend on another which isn't available to it.
 */
public class Genome
    implements Cloneable
{
    private static final long serialVersionUID = 87489987234876120L;

    /**
     * A functional interface which yields a "health" of a genome.
     *
     * <p>It's worth noting that factoring in the graph size to the
     * health function can possibly help in certain scenarios. This is
     * because it helps keep the search-space small and penalises the
     * genome for getting needlessly large (which can happen with
     * array functions). Having the graph size be too much of a
     * factor, however, can be counter-productive, as it prevents the
     * search-space from being adequately explored...
     */
    public static interface Health
    {
        /**
         * The best health value. This is {@code Double.MAX_VALUE}.
         */
        public static final double MAX_HEALTH = Double.MAX_VALUE;

        /**
         * The worst health value. This is zero.
         */
        public static final double MIN_HEALTH = 0;

        /**
         * Get the relative "health" of a genome.
         *
         * @param genome The genome to get the health of.
         *
         * @return The relative health of the given genome, never
         *         {@code Double.NaN}. A larger value is healthier
         *         than a smaller one. Valid ranges are between
         *         {@link MIN_HEALTH} and {@link MAX_HEALTH} inclusive.
         */
        public double healthOf(final Genome genome);

        /**
         * Get the context coverage of a genome.
         *
         * <p>This is the fraction of tested {@link Context}s which the genome
         * was able to yield a value for.
         *
         * @param genome The genome to get the coverage of.
         *
         * @return The fraction, between {@code 0.0} and {@code 1.0} inclusive.
         */
        public double coverage(final Genome genome);
    }

    // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -

    /**
     * How we uniquely number the genomes.
     */
    private static final AtomicLong ourNextId = new AtomicLong();

    /**
     * How we uniquely number the families.
     */
    private static final AtomicLong ourNextFamily = new AtomicLong();

    // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -

    /**
     * Our unique ID.
     */
    private long myId;

    /**
     * Our family's unique ID.
     */
    private long myFamily;

    /**
     * Our parent's ID.
     */
    private long myParentId;

    /**
     * Our generation; starting at 1.
     */
    private int myGeneration;

    /**
     * How we create new gene instances.
     */
    private final GeneFactory myFactory;

    /**
     * The maximum allowed genome size.
     */
    public final int myMaxSize;

    /**
     * The maximum allowed mutatioon factor.
     */
    public final double myMaxMutationFactor;

    /**
     * The mapping from handle to gene.
     */
    private Map<Gene.Handle,Gene> myGenes;

    /**
     * All the gene handles.
     */
    private List<Gene.Handle> myHandles;

    /**
     * The gene handles, keyed by return type.
     */
    private Map<Class<?>,List<Gene.Handle>> myHandlesByClass;

    /**
     * The types of our outputs.
     */
    private List<Class<?>> myOutputTypes;

    /**
     * The names of our outputs, of any.
     */
    private List<String> myOutputNames;

    /**
     * The outputs of our genome.
     */
    private Gene.Handle[] myOutputs;

    /**
     * The mutation factor.
     */
    private double myMutationFactor;

    // ----------------------------------------------------------------------

    /**
     * CTOR with various sensible defaults.
     *
     * @param factory      How genes can be created for inclusion in the genome.
     * @param outputTypes  The types of the values which this genome computes.
     * @param genes        The genes for this genome. The genome takes ownership.
     */
    public Genome(final GeneFactory      factory,
                  final List<Class<?>>   outputTypes,
                  final Collection<Gene> genes)
    {
        this(factory, genes, 100, outputTypes, null, 0.05);
    }

    /**
     * CTOR.
     *
     * @param factory           How genes can be created for inclusion in the genome.
     * @param genes             The gene for this genome. The genome takes
     *                          ownership of these.
     * @param maxSize           How many genes we may contain.
     * @param outputTypes       The types of the values which this genome computes.
     * @param maxMutationFactor The maximum mutation factor, between 0.0 and 1.0.
     */
    public Genome(final GeneFactory      factory,
                  final Collection<Gene> genes,
                  final int              maxSize,
                  final List<Class<?>>   outputTypes,
                  final List<String>     outputNames,
                  final double           maxMutationFactor)
    {
        // I am not a number! I am a-- oh wait...
        myId     = ourNextId    .getAndIncrement();
        myFamily = ourNextFamily.getAndIncrement();

        // If we are newly generated then no parent and we are the
        // first generation
        myParentId   = -1;
        myGeneration =  1;

        // How we create random genes
        myFactory = factory;

        // How big we may grow and how much we may mutate
        myMaxSize = maxSize;
        myMaxMutationFactor = Math.max(0.0, Math.min(1.0, maxMutationFactor));

        // All the genes
        myGenes = new HashMap<>(genes.size());
        genes.forEach(gene -> myGenes.put(gene.getHandle(), gene));

        // Handles...
        myHandles        = new ArrayList<>(myGenes.keySet());
        myHandlesByClass = new HashMap<>();
        for (Map.Entry<Gene.Handle,Gene> entry : myGenes.entrySet()) {
            myHandlesByClass.computeIfAbsent(entry.getValue().getReturnType(),
                                             klass -> new ArrayList<>())
                            .add(entry.getKey());
        }

        // Set up our parameter genes
        myMutationFactor = Math.random() * myMaxMutationFactor;

        // Init all the genes now
        myGenes.values().forEach(gene -> gene.init(this));

        // And set up the outputs
        myOutputTypes =
            Collections.unmodifiableList(new ArrayList<>(outputTypes));
        final List<String> tweakedOutputNames = new ArrayList<>();
        for (int i=0; i < outputTypes.size(); i++) {
            tweakedOutputNames.add(
                (outputNames != null && i < outputNames.size())
                    ? outputNames.get(i)
                    : "OUTPUT[" + i + "]"
            );
        }
        myOutputNames =
            Collections.unmodifiableList(tweakedOutputNames);
        myOutputs = new Gene.Handle[outputTypes.size()];
        for (int i=0; i < myOutputs.length; i++) {
            myOutputs[i] = pickAnyHandle(myOutputTypes.get(i));
        }
    }

    /**
     * The genome's globally unique ID.
     *
     * @return The ID.
     */
    public long getId()
    {
        return myId;
    }

    /**
     * The genome family's globally unique ID.
     *
     * @return The family ID.
     */
    public long getFamily()
    {
        return myId;
    }

    /**
     * The genome's parent's ID, if any.
     *
     * @return The ID of our parent genome, or {@code -1} if we have none.
     */
    public long getParentId()
    {
        return myParentId;
    }

    /**
     * Which generation this genome is from, starting at {@code 1}.
     *
     * @return Which generation this genome is.
     */
    public int getGeneration()
    {
        return myGeneration;
    }

    /**
     * Pick a handle at random from the genome.
     *
     * @param klass  The return type of the gene.
     *
     * @return The handle.
     */
    public Gene.Handle pickAnyHandle(final Class<?> klass)
    {
        final List<Gene.Handle> handles =
            myHandlesByClass.getOrDefault(klass, Collections.emptyList());
        if (handles.isEmpty()) {
            return null;
        }
        else {
            return handles.get((int)(Math.random() * handles.size()));
        }
    }

    /**
     * Get the gene with the given handle, if any. Do <b>NOT</b>
     * retain or mutate the value which you get back.
     *
     * @param handle The handle of the gene to get.
     *
     * @return The {@link Gene} for the given handle, or {@code nhll}
     *         if it does not exist.
     */
    public Gene get(final Gene.Handle handle)
    {
        return myGenes.get(handle);
    }

    /**
     * Get the gene at the given index. Do <b>NOT</b> retain or mutate
     * the value which you get back.
     *
     * @param index The index of the gene to get.
     *
     * @return The {@link Gene}.
     */
    public Gene get(final int index)
    {
        return get(myHandles.get(index));
    }

    /**
     * Get the gene handle at the given index.
     *
     * @param index The index of the gene to get.
     *
     * @return The {@link Gene.Handle}.
     */
    public Gene.Handle getHandle(final int index)
    {
        return myHandles.get(index);
    }

    /**
     * The number of genes in this genome.
     *
     * @return The number of genes.
     */
    public int getGenomeSize()
    {
        return myHandles.size();
    }

    /**
     * The maximum number of genes in this genome.
     *
     * @return Then maximum number of genes.
     */
    public int getGenomeMaxSize()
    {
        return myMaxSize;
    }

    /**
     * Get the graph size of this genome. This is the number of nodes
     * in all the evaluate trees and the criterion.
     *
     * @return The number of nodes.
     */
    public int getGraphSize()
    {
        int size = 0;
        for (Gene.Handle handle : myOutputs) {
            final Gene gene = myGenes.get(handle);
            if (gene != null) {
                size += gene.getGraphSize(this);
            }
        }
        return size;
    }

    /**
     * The number of outputs which this genome has.
     *
     * @return The number of outputs.
     */
    public int numOutputs()
    {
        return myOutputs.length;
    }

    /**
     * Get the type of the output for the given index.
     *
     * @param output The index of the required output.
     *
     * @return The type of the output.
     *
     * @throws IndexOutOfBoundsException If the given output was not
     *                                   in bounds.
     */
    public Class<?> getOutputType(final int output)
    {
        return myOutputTypes.get(output);
    }

    /**
     * Evaluate the genome in the given context.
     *
     * @param context The context to evaluate the genome in.
     * @param output  The index of the output to evaluate.
     *
     * @return {@code null} if the gene could not be evaluated.
     *
     * @throws IndexOutOfBoundsException If the given output was not
     *                                   in bounds.
     */
    public Object evaluate(final Context context, final int output)
        throws IndexOutOfBoundsException
    {
        final Gene gene = myGenes.get(myOutputs[output]);
        return (gene == null) ? null : gene.evaluate(context, this);
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
        return (that instanceof Genome) && ((Genome)that).myId == myId;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString()
    {
        final StringBuilder sb = new StringBuilder();

        sb.append("GENOME[").append(myId).append(']')
          .append('{');

        sb.append("FAMILY=").append(myFamily);

        sb.append(",PARENT_ID=").append(myParentId);

        sb.append(",GENERATION=").append(myGeneration);

        sb.append(",NUM_GENES=").append(getGenomeSize());
            
        sb.append(",GRAPH_SIZE=").append(getGraphSize());
            
        sb.append(",MUTATION_FACTOR={")
          .append(myMutationFactor)
          .append("}");

        for (int i=0; i < myOutputs.length; i++) {
            final Gene gene = myGenes.get(myOutputs[i]);
            final String output;
            if (gene == null) {
                output = "null";
            }
            else {
                output = gene.toString(this);
            }
            sb.append(',').append(myOutputNames.get(i)).append("={")
              .append(output)
              .append('}');
        }

        sb.append('}');

        return sb.toString();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Genome clone()
    {
        try {
            final Genome result = (Genome)super.clone();
            result.myId        = ourNextId.getAndIncrement();
            // myFamily is inherited
            result.myParentId  = myId;
            result.myGeneration++;
            result.myOutputs   = result.myOutputs.clone();
            // myOutputTypes is immutable so we can hold the cloned value
            result.myGenes     = new HashMap<>();
            for (Map.Entry<Gene.Handle,Gene> entry : myGenes.entrySet()) {
                result.myGenes.put(entry.getKey(), entry.getValue().clone());
            }
            result.myHandles = new ArrayList<>(result.myGenes.keySet());
            result.myHandlesByClass = new HashMap<>();
            for (Map.Entry<Class<?>,List<Gene.Handle>> entry :
                     myHandlesByClass.entrySet())
            {
                result.myHandlesByClass.put(entry.getKey(),
                                            new ArrayList<>(entry.getValue()));
            }
            return result;
        }
        catch (CloneNotSupportedException e) {
            // Should not happen
            throw new RuntimeException(e);
        }
    }

    // ----------------------------------------------------------------------

    /**
     * Perform a gene exchange with another Genome instance.
     *
     * <p>This only affects this instance, not the given one.
     *
     * @param that  The genome to copy from.
     */
    public void copyFrom(final Genome that)
    {
        // Walk the genes in the other genome and possibly pull them
        // into ours
        for (Map.Entry<Gene.Handle,Gene> entry : that.myGenes.entrySet()) {
            // Choose whether we want to take this gene
            if (Math.random() > myMutationFactor) {
                // Do nothing for this gene
                continue;
            }

            // We're going to take this gene. We need to get a copy
            // since we must never share instances between genomes.
            final Gene.Handle handle = entry.getKey();
            final Gene        gene   = entry.getValue().clone();

            // If we already have this gene then we replace our
            // existing copy
            if (myGenes.containsKey(handle)) {
                myGenes.put(handle, gene);
            }
            else if (myGenes.isEmpty() ||
                     Math.random() > (double)getGenomeSize() / myMaxSize)
            {
                // Copy in the gene directly
                myGenes         .put(handle, gene);
                myHandles       .add(handle);
                myHandlesByClass.computeIfAbsent(gene.getReturnType(),
                                                 klass -> new ArrayList<>())
                                .add(handle);
            }
            else {
                // Replace an existing gene with this one. First,
                // choose a random gene and junk it.
                removeRandomGene();

                // And then add in the new gene
                myGenes         .put(handle, gene);
                myHandles       .add(handle);
                myHandlesByClass.computeIfAbsent(gene.getReturnType(),
                                                 klass -> new ArrayList<>())
                                .add(handle);
            }
        }
    }

    /**
     * Mutate the genome.
     */
    public void mutate()
    {
        // First, mutate the mutation factor
        myMutationFactor =
            Math.max(
                0.0,
                Math.min(
                    myMaxMutationFactor,
                    myMutationFactor +
                        (myMaxMutationFactor * 0.1 * (Math.random() - 0.5))
                )
            );

        // Mutate all the genes
        myGenes.forEach(
            (handle, gene) -> gene.mutate(this,
                                          myMutationFactor)
        );

        // Possibly insert or remove a random gene
        if (Math.random() < myMutationFactor) {
            // Which?
            if (Math.random() >= 0.5) {
                if (!myGenes.isEmpty()) {
                    removeRandomGene();
                }
            }
            else {
                if (myGenes.size() < myMaxSize) {
                    final Gene gene = myFactory.generate();
                    gene.init(this);
                    myGenes         .put(gene.getHandle(), gene);
                    myHandles       .add(gene.getHandle());
                    myHandlesByClass.computeIfAbsent(gene.getReturnType(),
                                                     klass -> new ArrayList<>())
                                    .add(gene.getHandle());
                }
            }
        }
    }

    // ----------------------------------------------------------------------

    /**
     * Get the graph size for a handle.
     */
    private void getGraphHandles(final Gene.Handle       handle,
                                 final List<Gene.Handle> dest)
    {
        final Gene gene = myGenes.get(handle);
        if (gene == null) {
            dest.add(handle);
        }
        else {
            gene.getGraphHandles(this, dest);
        }
    }

    /**
     * Remove a random gene.
     */
    private void removeRandomGene()
    {
        if (!myGenes.isEmpty()) {
            final int index = (int)(Math.random() * myHandles.size());
            final Gene.Handle handle = myHandles.remove(index);
            final Gene        gene   = myGenes  .remove(handle);
            myHandlesByClass.getOrDefault(gene.getReturnType(),
                                          Collections.emptyList())
                            .remove(handle);
        }
    }
}
