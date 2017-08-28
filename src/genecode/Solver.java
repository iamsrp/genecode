package genecode;

import genecode.StringUtil;
import genecode.Context.Identifier;
import genecode.function.Function;
import genecode.gene.Accessor;
import genecode.gene.Gene;
import genecode.gene.GeneFactory;

import java.util.Arrays;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A class which attempts to generate {@link Genome}s which are
 * solutions to a given function, or as near as it can muster.
 *
 * <p>The implementation of this class may, or may not, work for your
 * particular problem set. Think of it more as an example of how to
 * write a solver.
 */
public class Solver
{
    /**
     * Our logger.
     */
    public static final Logger LOG = Logger.getLogger(Solver.class.getName());

    // ----------------------------------------------------------------------

    /**
     * A variable over which the solver solves.
     */
    public static class Variable<T>
    {
        /**
         * The ID of the variable.
         */
        private final Identifier<T> myId;

        /**
         * The value for the variable.
         */
        private final T[] myValues;

        /**
         * CTOR.
         *
         * @param identifier  The variable's {@link Identifier}.
         * @param values      The variable's values.
         */
        public Variable(final Identifier<T> identifier,
                        final T[]           values)
        {
            Objects.requireNonNull(identifier, "Null identifier");
            myId     = identifier;
            myValues = values;
        }

        /**
         * The ID of the variable.
         *
         * @return The Identifier.
         */
        public Identifier<T> getIdentifier()
        {
            return myId;
        }

        /**
         * The type that we return.
         *
         * @return The return type.
         */
        public Class<?> getType()
        {
            return myValues.getClass().getComponentType();
        }

        /**
         * The value for the variable at the given index.
         *
         * @param index The index of the variable.
         *
         * @return The value at the given index.
         */
        public Object get(final int index)
        {
            return myValues[index];
        }

        /**
         * The number of values we have.
         *
         * @return The number of values.
         */
        public int count()
        {
            return myValues.length;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public boolean equals(final Object object)
        {
            if (!(object instanceof Variable)) {
                return false;
            }

            final Variable that = (Variable)object;
            return (myId.equals(that.myId)  &&
                    Arrays.equals(myValues, that.myValues));
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public int hashCode()
        {
            return (myId   .hashCode()        ^
                    Arrays .hashCode(myValues));
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public String toString()
        {
            return myId + Arrays.toString(myValues);
        }
    }

    /**
     * The current state of the solver. This allows implementers of
     * the {@link SolverContext} to access the current values of the
     * {@link Variable}s etc.
     */
    public interface SolverState
    {
        /**
         * Get the current value of a given {@link Variable}.
         *
         * @param variable The variable to get the value of.
         *
         * @return The value of the variable.
         */
        public Object access(final Variable variable);
    }

    /**
     * The context used by clients of the {@link Solver}. This looks like the
     * regular {@link Context} except that it passes along extra information
     * about the solver's state current.
     */
    public static class SolverContext
    {
        /**
         * Access the value for a given {@link Identifier} from the
         * context.
         *
         * @param state The current state of the solver.
         * @param id    The identifier to get the value for.
         *
         * @return {@code Double.NaN} if no such value exists.
         */
        public Double access(final SolverState    state,
                             final Identifier<?>  id)
        {
            return Double.NaN;
        }

        /**
         * Access the value for a given {@link Identifier} from the context,
         * parameterised by the given double values.
         *
         * @param state  The current state of the solver.
         * @param id     The identifier to get the value for.
         * @param params The values which may parameterise the id.
         *
         * @return {@code Double.NaN} if no such value exists.
         */
        public Double access(final SolverState    state,
                             final Identifier<?>  id,
                             final Object...      params)
        {
            return (params.length == 0) ? access(state, id) : Double.NaN;
        }
    }

    /**
     * Our gene factory.
     */
    private class Factory
        implements GeneFactory
    {
        /**
         * The genes which we may draw from.
         */
        private final List<Supplier<Gene>> myGeneSuppliers;

        /**
         * CTOR.
         *
         * @param suppliers The gene generators
         */
        public Factory(final Collection<Supplier<Gene>> suppliers)
        {
            myGeneSuppliers = (suppliers == null) ? Collections.emptyList()
                                                  : new ArrayList<>(suppliers);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        @SuppressWarnings("unchecked")
        public Gene generate()
        {
            final int total = myGeneSuppliers.size() + myVariables.size();
            final int index =
                Math.max(
                    0,
                    Math.min(
                        total - 1,
                        (int)Math.floor(Math.random() * total)
                    )
                );
            if (index < myGeneSuppliers.size()) {
                return myGeneSuppliers.get(index).get();
            }
            else {
                final Variable<?> v = myVariables.get(index - myGeneSuppliers.size());
                return new Accessor(v.getIdentifier(), v.getType());
            }
        }
    }

    /**
     * The context we use for solving with.
     */
    private class ContextIterator
        extends    Context
        implements SolverState
    {
        /**
         * The current step index for computing the variable values.
         */
        private final int[] myVariableIndices;

        /**
         * The values.
         */
        private final Object[] myValues;

        /**
         * The current ID.
         */
        private long myId;

        /**
         * CTOR.
         */
        public ContextIterator()
        {
            myVariableIndices = new int   [myVariables.size()];
            myValues          = new Object[myVariables.size()];
            reset();
        }

        /**
         * Get the number if points which this iterator will produce.
         */
        public int count()
        {
            // If not variables then no values
            if (myVariables.isEmpty()) {
                return 0;
            }

            // Otherwise it's the product of ranges
            int count = 1;
            for (int i=0; i < myVariables.size(); i++) {
                count *= myVariables.get(i).count();
            }
            return count;
        }

        /**
         * Reset the iterator to be like new.
         */
        public void reset()
        {
            for (int i=0; i < myVariableIndices.length; i++) {
                // -1 for the very first next() call
                myVariableIndices[i] = (i==0) ? -1 : 0;
            }
        }

        /**
         * Step on to the next set of variable values.
         *
         * @return Whether there are any valid values left.
         */
        public boolean next()
        {
            try {
                // Increment...
                for (int i=0; i < myVariableIndices.length; i++) {
                    myVariableIndices[i]++;
                    if (myVariableIndices[i] < myVariables.get(i).count()) {
                        return true;
                    }
                    else {
                        myVariableIndices[i] = 0;
                    }
                }
                return false;
            }
            finally {
                // Recompute the values and ID
                myId = 0;
                for (int i=0; i < myVariables.size(); i++) {
                    // Compute the values
                    final Variable variable = myVariables.get(i);
                    myValues[i] = variable.get(myVariableIndices[i]);

                    // Accumulate into the ID
                    myId *= variable.count();
                    myId += myVariableIndices[i];

                    // Debugging...
                    if (LOG.isLoggable(Level.FINEST)) {
                        LOG.finest(variable + " -> " + myValues[i]);
                    }
                }
            }
        }

        /**
         * Get the values as an array.
         */
        public Object[] getValues()
        {
            return myValues;
        }
        
        /**
         * {@inheritDoc}
         */
        @Override
        public long getId()
        {
            return myId;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public Object access(final Variable variable)
        {
            for (int i=0; i < myVariables.size(); i++) {
                if (myVariables.get(i).equals(variable)) {
                    return myValues[i];
                }
            }
            return Double.NaN;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public Object access(final Identifier<?> id)
        {
            // See if it's one of our variables
            for (int i=0; i < myVariables.size(); i++) {
                if (myVariables.get(i).getIdentifier().equals(id)) {
                    return myValues[i];
                }
            }

            // Nope, defer to the given context
            return myContext.access(this, id);
        }
    }

    /**
     * How we determine the health of a genome.
     */
    private class Health
        implements Genome.Health
    {
        /**
         * The cached health values.
         */
        private final Map<Genome,double[]> myCache = new ConcurrentHashMap<>();

        /**
         * The amount of noise to add to health values.
         */
        private double myHealthNoise = 0.0;

        /**
         * Clear any cached data.
         */
        public void clear()
        {
            myCache.clear();
        }

        /**
         * Set the amount of noise to add to the health value.
         */
        public void setHealthNoise(final double healthNoise)
        {
            myHealthNoise = Math.max(0.0, Math.min(1.0, healthNoise));
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public double healthOf(final Genome genome)
        {
            synchronized (genome) {
                double[] values = myCache.get(genome);
                if (values == null) {
                    values = computeHealth(genome);
                    myCache.put(genome, values);
                }
                return values[0];
            }
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public double coverage(final Genome genome)
        {
            synchronized (genome) {
                double[] values = myCache.get(genome);
                if (values == null) {
                    values = computeHealth(genome);
                    myCache.put(genome, values);
                }
                return values[1];
            }
        }

        /**
         * Compute a genome's health.
         */
        private double[] computeHealth(final Genome genome)
        {
            // Cumulative over all the outputs
            int    totalContexts  = 0;
            int    totalCount     = 0;
            double totalMatchness = 0.0; // Yeah, I know...

            // Handle all the outputs
            for (int output = 0; output < genome.numOutputs(); output++) {
                final ContextIterator itr     = new ContextIterator();
                final int             numCxts = itr.count();
                final Object[]        targets = new Object[numCxts];
                final Object[]        values  = new Object[numCxts];
                int count = 0;
                for (itr.reset(); itr.next(); /*nothing*/) {
                    final Object target = myFunctions[output].call(itr.getValues());
                    final Object value  = genome.evaluate(itr, output);
                    if (target != null && value != null) {
                        targets[count] = target;
                        values [count] = value;
                        count++;
                    }
                }

                // Anything?
                if (count == 0) {
                    continue;
                }

                double matchness;
                if (Number.class.isAssignableFrom(genome.getOutputType(output))) {
                    // Use R^2 to determine the health. First get all the
                    // values we need. This isn't quite perfect since it's not
                    // a linear regression but it's a reasonable estimation of
                    // error for our purposes.
                    matchness = r2(targets, values, count);
                }
                else if (String.class.equals(genome.getOutputType(output))) {
                    // Simple similarity function
                    matchness = similarity(targets, values, count);
                }
                else {
                    matchness = Double.NaN;
                }

                // Anything?
                if (Double.isNaN(matchness)) {
                    continue;
                }

                // Now we account for a few things. We try to do this
                // in such a way that it won't be too overwhelming to
                // the matchness value, These are really meant to
                // nuance the output.

                // We throw in the graph size so as to try to
                // prevent solutions from getting needlessly
                // complex. We use the below formula to prevent it
                // overwhelming the result.
                if (genome.getGraphSize() > 1 && mySizePenaltyFactor > 0) {
                    final double nodes = genome.getGraphSize();
                    final double size  = genome.getGenomeMaxSize();
                    final double factor = 10;
                    matchness /= Math.log(nodes + size / mySizePenaltyFactor) /
                                 Math.log(        size / mySizePenaltyFactor);
                }

                // And also deem genomes which apply to more of the
                // contexts as better. This is effectvely emulating
                // the idea that viruses which get into more cells can
                // breed more.
                final double coverage;
                if (numCxts > 0) {
                    coverage =
                        Math.max(0.0, Math.min(1.0, (double)count / numCxts));
                }
                else {
                    coverage = 0.0;
                }

                // We also factor the coverage into the health. This
                // stops overly specialised genomes from dominating
                // things too much.
                matchness *= (1.0 - myCoverageFactor) + myCoverageFactor * coverage;

                // Still anything?
                if (!Double.isNaN(matchness)) {
                    totalMatchness += matchness;
                    totalCount     += count;
                    totalContexts  += numCxts;
                }
            }

            // Anything?
            if (totalCount == 0) {
                return ZERO_COUNT_VALUE;
            }

            // How much coverage did we have?
            final double totalCoverage = 
                Math.max(0.0, Math.min(1.0, (double)totalCount / totalContexts));

            // Add some noise to the health, to allow escape from local maxima
            totalMatchness =
                ((1.0 - myHealthNoise) * totalMatchness +
                 myHealthNoise         * ThreadLocalRandom.current().nextDouble());

            // Talk to the animals
            if (LOG.isLoggable(Level.FINEST)) {
                LOG.finest(
                    "Health [" +
                        totalMatchness + ", " +
                        totalCoverage  +
                    "] for " + genome
                );
            }

            // And give back the values, suitably capped
            return new double[] {
                Double.isNaN(totalMatchness) ? MIN_HEALTH
                                             : Math.min(MAX_HEALTH,
                                                        totalMatchness),
                totalCoverage
            };
        }

        /**
         * Compute the R^2 for a pair of arrays of {@link Number}s.
         *
         * @return Double.NaN if it could not be computed.
         */
        private double r2(final Object[] targets,
                          final Object[] values,
                          final int      length)
        {
            // Sanity
            if (targets == null || values == null) {
                return Double.NaN;
            }

            // How many sane data-points
            int count = 0;
            double sum = 0;
            for (int i=0; i < length; i++) {
                final Object target = targets[i];
                final Object value  = values [i];
                if (target instanceof Number &&
                    value  instanceof Number)
                {
                    final double t = ((Number)target).doubleValue();
                    final double v = ((Number)value ).doubleValue();
                    if (!Double.isNaN(t) && !Double.isNaN(v)) {
                        count++;
                        sum += v;
                    }
                }
            }

            // Anything?
            if (count == 0) {
                return Double.NaN;
            }

            // Need the mean of the actual values
            final double mean = sum / count;

            // Now compute the values needed for R^2
            double ssTot = 0.0;
            double ssRes = 0.0;
            for (int i=0; i < values.length; i++) {
                final Object target = targets[i];
                final Object value  = values [i];
                if (target instanceof Number &&
                    value  instanceof Number)
                {
                    final double t = ((Number)target).doubleValue();
                    final double v = ((Number)value ).doubleValue();
                    if (!Double.isNaN(t) && !Double.isNaN(v)) {
                        ssTot += (t - mean) * (t - mean);
                        ssRes += (t - v   ) * (t - v   );
                    }
                }
            }

            // Compute the actual R^2 now
            return Math.max(MIN_HEALTH, 1.0 - (ssRes / ssTot));
        }

        /**
         * Compute the similarity between a pair of arrays of {@link String}s.
         *
         * @return Double.NaN if it could not be computed.
         */
        private double similarity(final Object[] targets,
                                  final Object[] values,
                                  final int      length)
        {
            // Sanity
            if (targets == null || values == null || length == 0) {
                return Double.NaN;
            }

            // Compute it for each pair
            double sum = 0;
            for (int i=0; i < length; i++) {
                // Cast here
                final Object objT = targets[i];
                final Object objV = values [i];

                if (objT instanceof String && objV instanceof String) {
                    sum +=
                        Math.min(
                            1.0,
                            Math.max(
                                0.0,
                                StringUtil.distance(
                                    (String)objT,
                                    (String)objV
                                )
                            )
                        );
                }
            }

            // Give it back, suitably normalised and inverted
            final double distance = sum / length;
            return 1.0 - distance;
        }
    }
    private static final double[] ZERO_COUNT_VALUE =
        new double[] { Genome.Health.MIN_HEALTH, 0.0};

    // ----------------------------------------------------------------------
    
    /**
     * The number of random genes to add to the genome, in addition to
     * the variable accessors.
     */
    private final int myNumGenes;

    /**
     * How much to penalize large genomes. This is to prevent genomes
     * getting needlessly large.
     */
    private final double mySizePenaltyFactor;

    /**
     * The variables we are working over.
     */
    private final List<Variable> myVariables;

    /**
     * The function we are trying to compute.
     */
    private final Function[] myFunctions;

    /**
     * The client's context used for evaluating the genomes.
     */
    private final SolverContext myContext;

    /**
     * How much coverage factors into health.
     */
    private final double myCoverageFactor;

    /**
     * The Biomes we are using.
     */
    private final List<Biome> myBiomes;

    /**
     * The gene factor to use for creating genomes etc.
     */
    private final GeneFactory myGeneFactory;

    /**
     * The max number of genes in a genome.
     */
    private final int myMaxGenomeSize;

    /**
     * The max mutation factor of a genome.
     */
    private final double myMaxMutationFactor;

    /**
     * The health function which we're using.
     */
    private final Health myHealthComputer;

    // ----------------------------------------------------------------------

    /**
     * CTOR.
     *
     * @param variables         The variables over which we are solving.
     * @param function          The function which we are solving for.
     * @param context           The context for accessing values from.
     * @param coverageFactor    How much coverage factors into health.
     * @param sizePenaltyFactor How much to penalize large genomes.
     * @param geneSuppliers     How we create new gene instances.
     * @param numBiomes         How many biomes to use to solve with.
     * @param biomeSize         The number of genomes in each biome.
     */
    public Solver(final List<Variable>             variables,
                  final Function                   function,
                  final SolverContext              context,
                  final double                     coverageFactor,
                  final double                     sizePenaltyFactor,
                  final Collection<Supplier<Gene>> geneSuppliers,
                  final int                        numBiomes,
                  final int                        biomeSize)
    {
        // Sanity checks
        Objects.requireNonNull(variables, "Null variables");
        Objects.requireNonNull(function,  "Null function");
        Objects.requireNonNull(context,   "Null context");
        if (numBiomes <= 0) {
            throw new IllegalArgumentException(
                "Given a non-positive number of biomes"
            );
        }
        if (variables.isEmpty()) {
            throw new IllegalArgumentException(
                "Given an empty list of variables"
            );
        }
        for (int i=0; i < variables.size(); i++) {
            final Variable v1 = variables.get(i);
            if (v1 == null) {
                throw new NullPointerException(
                    "Null variable in list: " + variables
                );
            }
            for (int j = i+1; j < variables.size(); j++) {
                final Variable v2 = variables.get(j);
                if (v1.getIdentifier().equals(v2)) {
                    throw new IllegalArgumentException(
                        "Repeated identifier, " + v1 + ", in list: " + variables
                    );
                }
            }
        }

        // Assign
        myNumGenes          = 25;
        myVariables         = new ArrayList<>(variables);
        myFunctions         = new Function[] { function };
        myContext           = context;
        myCoverageFactor    = Math.max(0.0, Math.min(1.0, coverageFactor));
        mySizePenaltyFactor = Math.max(0.0, Math.min(1.0, sizePenaltyFactor));
        myGeneFactory       = new Factory(geneSuppliers);
        myMaxGenomeSize     = 500;
        myMaxMutationFactor = 0.10;

        myBiomes = new ArrayList<>(numBiomes);
        for (int i=0; i < numBiomes; i++) {
            myBiomes.add(new Biome(biomeSize, this::generate));
        }

        myHealthComputer = new Health();
    }

    /**
     * Step the solver. This will compute the next generation for each
     * biome using zero noise.
     */
    public void step()
    {
        step(1, 0.0);
    }

    /**
     * Step the solver using the given number of worker threads. This will
     * compute the next generation for each biome.
     *
     * <p>The noise parameter will allow the solver to peturb the computed
     * health values. The more noise you have the less chance you have of
     * getting trapped in a local maximum. However, more noise also means that
     * you will not converge. Different techniques use this parameter in
     * different ways (cf simulated annealing).
     *
     * @param numWorkers   How many worker threads to spawn.
     * @param healthNoise  How much noise to add to the health values (between 
     *                     zero and one).
     */
    public void step(final int numWorkers, final double healthNoise)
    {
        LOG.fine("Stepping on");

        // Flush the health cache and set the noise level
        myHealthComputer.clear();
        myHealthComputer.setHealthNoise(healthNoise);

        // If we have more than one worker thread then we use them to pre-cache
        // the healths
        if (numWorkers > 1) {
            precacheHealth(numWorkers);
        }

        // Now step all the biomes
        for (int i=0; i < myBiomes.size(); i++) {
            myBiomes.get(i).nextGeneration(myHealthComputer);
        }

        // And precache again since callers will likely want the health value
        if (numWorkers > 1) {
            precacheHealth(numWorkers);
        }
    }

    /**
     * Get the list of Biomes within the solver. You may modify the
     * contents of this list if you so desire.
     *
     * @return The biomes.
     */
    public List<Biome> getBiomes()
    {
        return myBiomes;
    }

    /**
     * Get the health of the given genome.
     *
     * @param genome The genome to get the health of.
     *
     * @return The health.
     */
    public double healthOf(final Genome genome)
    {
        return (genome == null) ? Genome.Health.MIN_HEALTH
                                : myHealthComputer.healthOf(genome);
    }

    /**
     * Get the coverage of the given genome.
     *
     * @param genome The genome to get the coverage of.
     *
     * @return The coverage.
     */
    public double coverage(final Genome genome)
    {
        return (genome == null) ? 0.0 : myHealthComputer.coverage(genome);
    }

    /**
     * The number of genes we add to new genomes.
     *
     * @return The number of genes which we add to new genomes.
     */
    protected int getNumGenes()
    {
        return myNumGenes;
    }

    /**
     * The gene factory.
     *
     * @return The instance's {@link GeneFactory}.
     */
    protected GeneFactory getGeneFactory()
    {
        return myGeneFactory;
    }

    /**
     * How we create a new random genome.
     *
     * @return The resultant genome.
     */
    @SuppressWarnings("unchecked")
    protected Genome generateGenome()
    {
        // Create the set of genes
        final List<Gene> genes = new ArrayList<>();

        // Add accessors for our variables
        for (int i=0; i < myVariables.size(); i++) {
            genes.add(new Accessor(myVariables.get(i).getIdentifier(),
                                   myVariables.get(i).getType()));
        }
            
        // Add random genes
        for (int j=0; j < myNumGenes; j++) {
            genes.add(myGeneFactory.generate());
        }

        // What we want
        final List<Class<?>> outputTypes = new ArrayList<>();
        for (Function function : myFunctions) {
            outputTypes.add(function.getReturnType());
        }

        // Hand back the genome with these genes in it
        return new Genome(myGeneFactory,
                          genes,
                          myMaxGenomeSize,
                          outputTypes,
                          myMaxMutationFactor);
    }

    /**
     * Generate a gene, method to bind with which will call overridden
     * generateGenome() methods correctly.
     */
    private Genome generate()
    {
        return generateGenome();
    }

    /**
     * Precache the health values using the given number of worker threads.
     */
    private void precacheHealth(final int numWorkers)
    {
        // Don't bother if we don't have any worker threads
        if (numWorkers <= 1) {
            return;
        }

        // Use this index to figure out which genome in all the biomes to work
        // on. We'll spawn a bunch of computer threads to do the work.
        final AtomicInteger index = new AtomicInteger();
        final Thread[] computers = new Thread[numWorkers];
        for (int i=0; i < computers.length; i++) {
            computers[i] = new Thread(() -> {
                while (true) {
                    // Compute the health of the gene at this index. Assume
                    // we're done until we find out otherwise.
                    int idx = index.getAndIncrement();
                    boolean done = true;
                    for (int j=0; j < myBiomes.size(); j++) {
                        // See if the index falls in here
                        final Biome        biome   = myBiomes.get(j);
                        final List<Genome> genomes = biome.getGenomes();
                        if (idx >= genomes.size()) {
                            // Nope, we'll look in the next ome
                            idx -= genomes.size();
                        }
                        else {
                            // Yes, compute
                            myHealthComputer.healthOf(genomes.get(idx));

                            // So we're still working...
                            done = false;
                        }
                    }

                    // Done?
                    if (done) {
                        return;
                    }
                }
            });

            // Spawn it
            computers[i].start();
        }

        // And wait...
        for (Thread computer : computers) {
            try {
                computer.join();
            }
            catch (InterruptedException e) {
                // Not expected
                throw new RuntimeException(e);
            }
        }
    }
}
