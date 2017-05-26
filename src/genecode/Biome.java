package genecode;

import genecode.gene.Gene;

import java.io.Serializable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * The biome, where all the genomes live.
 *
 * <p>This is paramterised by the following:<ul>
 *   <li><i>Size</i> The number of genomes it holds.</li>
 *
 *   <li><i>SkewFactor</i> The skew factor for breeding. One is the
 *   maximum skew, zero is no skew. A good value is probably 0.75.</li>
 *
 *   <li><i>BreedFactor</i> The factor for breeding vs asexual
 *   reproduction. Zero means purely asexual reproduction and one means
 *   purely breeding.</li>
 *
 *   <li><i>CullFactor</i> The cull factor when creating a new
 *   generation as the population approaches <i>Size</i>. Zero means
 *   no culling, one means cull everything. A good value is probably
 *   0.5. The least "fit" genomes will be culled first.</li>
 *
 *   <li><i>NewFactor</i> The new factor when creating a new
 *   generation. Zero means all new genomes are spawned by existing
 *   ones; one means all new genomes are freshly generated.</li>
 * </ul>
 *
 * <p>Operations on the Biome and its contents are not thread-safe.  Typically
 * you may want to use multiple biome instances over a number of threads.
 */
public class Biome
    implements Serializable
{
    /**
     * Our logger.
     */
    public static final Logger LOG = Logger.getLogger(Biome.class.getName());

    /**
     * Our size.
     */
    private final int mySize;

    /**
     * The skew factor for breeding. One is the maximum skew, zero is no skew. A
     * good value is probably 0.75.
     */
    private final double myBreedSkew;

    /**
     * The factor for breeding vs asexual reproduction. Zero means purely
     * asexual reproduction and one means purely breeding.
     */
    private final double myBreedFactor;

    /**
     * The cull factor when creating a new generation. Zero means no culling and
     * one means cull everything. The least "fit" genomes will be culled first.
     */
    private final double myCullFactor;

    /**
     * The new factor when creating a new generation. Zero means no generated
     * genomes and one means all generated ones.
     */
    private final double myNewFactor;

    /**
     * The genomes which we contain.
     */
    private volatile List<Genome> myGenomes;

    /**
     * How we make new genome instances.
     */
    private final Supplier<Genome> myMaker;

    // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -

    /**
     * CTOR with some sensible defaults.
     *
     * @param size  The number of genomes which we will contain.
     * @param maker  How the biome can generate new genome instances.
     */
    public Biome(final int              size,
                 final Supplier<Genome> maker)
    {
        this(size,
             0.25,
             0.10,
             0.50,
             0.50,
             Collections.emptyList(),
             maker);
    }

    /**
     * CTOR.
     *
     * @param size        The number of genomes which we contain.
     * @param breedSkew   The health skew when randomly picking a mate.
     * @param breedFactor How much reproduction involves breeding.
     * @param cullFactor  How much of the biome to cull to make room when
     *                    creating new genomes or breeding.
     * @param newFactor   The fraction of the next generation which are newly
     *                    generated genomes.
     * @param seed        The initial genomes for the biome. Additional genomes
     *                    will be created with the maker to pad out to the
     *                    size, if required.
     * @param maker       How the biome can generate new genome instances.
     */
    public Biome(final int                size,
                 final double             breedSkew,
                 final double             breedFactor,
                 final double             cullFactor,
                 final double             newFactor,
                 final Collection<Genome> seed,
                 final Supplier<Genome>   maker)
    {
        // Set up state
        mySize        = Math.max(0, size);
        myBreedSkew   = Math.min(1.0, Math.max(0.0, breedSkew));
        myBreedFactor = Math.min(1.0, Math.max(0.0, breedFactor));
        myCullFactor  = Math.min(1.0, Math.max(0.0, cullFactor));
        myNewFactor   = Math.min(1.0, Math.max(0.0, newFactor));
        myGenomes     = new ArrayList<>(mySize);
        myMaker       = maker;

        // Populate the biome, starting with any seed
        for (Genome genome : seed) {
            if (myGenomes.size() >= mySize) {
                break;
            }
            else {
                myGenomes.add(genome);
            }
        }
        while (myGenomes.size() < mySize) {
            myGenomes.add(maker.get());
        }
    }

    /**
     * Get the list of genomes within the biome. You may modify the
     * contents of this list, if you so desire.
     *
     * @return All the genomes in this biome.
     */
    public List<Genome> getGenomes()
    {
        return myGenomes;
    }

    /**
     * Create the next generation within this biome. This will potentially cull
     * a portion of the biome in order to make room for new genomes.
     *
     * @param genomeHealth How to determine the health of a particular
     *                     genome instance.
     */
    public void nextGeneration(final Genome.Health genomeHealth)
    {
        // Do nothing if we have no genomes
        if (myGenomes.size() == 0) {
            return;
        }

        // First, sort the genomes we have. We invert the sort so that the
        // healthiest ones are at the front. Precompute all the healths to speed
        // things up.
        final Map<Genome,Double> healths = new HashMap<>();
        for (int i=0; i < myGenomes.size(); i++) {
            final Genome genome = myGenomes.get(i);
            healths.put(genome, genomeHealth.healthOf(genome));
        }
        Collections.sort(
            myGenomes,
            (a, b) -> -compare(a, b, genome -> healths.get(genome))
        );

        // How much of the current generation to copy so that we have
        // space for the next generation
        final int copyTo =
            Math.min(myGenomes.size(),
                     (int)((1.0 - myCullFactor) * mySize));

        // Talking 'bout my (new) generation...
        final List<Genome> genomes = new ArrayList<>(myGenomes.size());

        // The next generation. We copy across values but sampling in such a way
        // as to bias the top (healthiest) of the current generation. We do this
        // by figuring out the power which we need to raise copyTo to so as to
        // get the current size. If we were sampling 5 from 25 then we'd get 2
        // as the power and would sample 0, 1, 4, 9 and 16. This means that we
        // generally try to cull some healthy genomes in favour of less healthy
        // ones; that should (hopefully) reduce stagnation and getting trapped
        // in local maximas.
        if (copyTo > 0) {
            final double pow = Math.log(myGenomes.size()) / Math.log(copyTo);
            for (int i=0; i < copyTo; i++) {
                // Floor the power to the integer value
                final int j = (int)Math.pow(i, pow);
                genomes.add(myGenomes.get(j));
            }
        }

        // Shuffle the new genomes so that we have some randomness in the
        // breeding that we do below
        Collections.shuffle(genomes);

        // How many genomes to create via reproduction. The (mySize-copyTo)
        // value is how much space we have left in the biome.
        final int reproductionCount = 
            Math.max(0, (int)((1.0 - myNewFactor) * (mySize - copyTo)));

        // See how many of the genomes we are breeding and how many we are
        // asexually spawning
        final int breedTo = (int)(genomes.size() * myBreedFactor);

        // Now we can breed
        int bred = 0;
        for (int i=0;
             i < copyTo && bred < reproductionCount;
             i++)
        {
            // See if this one may breed. We only let it do so if it was able to
            // apply itself to "enough" contexts. (This compares with a virus
            // finding a host cell within which it may reproduce.)
            final double coverage = genomeHealth.coverage(genomes.get(i));
            if (coverage < Math.random()) {
                continue;
            }

            // Take a copy of a genome
            final Genome genome = genomes.get(i).clone();

            // Mutate it a bit
            genome.mutate();

            // Possibly breed a bit
            if (i < breedTo) {
                // Grab a random genome from the original set. We use the skew
                // to potentially shift the random number to the healthier
                // genomes.
                final double fraction =
                    1.0 - Math.max(
                        0.0,
                        Math.min(
                            1.0,
                            Math.pow(Math.random(), 1.0 - myBreedSkew)
                        )
                    );
                final Genome partner =
                    myGenomes.get((int)(myGenomes.size() * fraction));

                // And breed with it
                genome.copyFrom(partner);
            }

            // Finally add it to the mix and remember that we bred one
            genomes.add(genome);
            bred++;
        }

        // Now pad with generated genomes
        while (genomes.size() < mySize) {
            genomes.add(myMaker.get());
        }
        
        // And assign it over
        myGenomes = genomes;
    }

    // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -

    /**
     * Compare two genomes using the given health interface. Ties will be broken
     * with the graph size (fewer nodes is better).
     */
    private int compare(final Genome                  a,
                        final Genome                  b,
                        final Function<Genome,Double> health)
    {
        if (a == null && b == null) {
            return 0;
        }
        else if (a == null) {
            return 1;
        }
        else if (b == null) {
            return -1;
        }
        else {
            final int cmp = Double.compare(health.apply(a),
                                           health.apply(b));
            if (cmp == 0) {
                return -Integer.compare(a.getGraphSize(),
                                        b.getGraphSize());
            }
            else {
                return cmp;
            }
        }
    }
}
