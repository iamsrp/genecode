/**
 * <p>Genecode is a genetic algorithm system modelled after the way
 * that biological viruses work.</p>
 *
 * <h2>Overview</h2>
 *
 * <p>In the real world a virus has the following properties:</p><ul>
 *
 *  <li><i>Genetic</i> &mdash; The mechanics of a virus are encoded in
 *  either RNA or DNA. (The machinery of the host cell is taken over
 *  in order to put the encoded instructions into action.) Proteins
 *  may be created from the virus's genetic material.</li>
 *
 *  <li><i>Specificity</i> &mdash; A virus will tend to specifically
 *  bind to a single type of host cell.</li>
 *
 *  <li><i>Asexual reproduction</i> &mdash; When a virus reproduces it
 *  creates a near-exact copy of itself using the host cell's
 *  machinery. However, as with all genetic reproduction, mutations
 *  may occur during this process.</li>
 *
 *  <li><i>Recombination</i> &mdash; While viruses reproduce
 *  asexually, they may exchange genetic material with one another, or
 *  with their host cell, copying in foreign RNA/DNA to their own
 *  genome.</li>
 *
 * </ul>
 *
 * <h2>Components</h2>
 *
 * <p>The genecode system is comprised of the following parts which
 * are based on this real-world model.</p>
 *
 * <h3>{@link genecode.Biome}</h3>
 *
 * <p>The biome contains various individual {@link genecode.Genome}
 * instances.  As well as holding all the genomes it is responsible
 * for their generational life-cycle. This life-cycle is parameterised
 * by various factors which determine how genomes in the biome are
 * created or destroyed.</p>
 *
 * <p>With each new generation some genomes are removed and some are
 * created. New genomes are either created fresh or they are produced
 * via reproduction from the existing genomes. During the reproductive
 * cycle genomes may take genetic material from other genomes and use
 * it to insert or overwrite their own contents.</p>
 *
 * <p>The reproductive cycle uses a {@link genecode.Genome.Health}
 * instance, supplied by the caller, in order to determine which
 * genomes are the most fit, and should thus be preserved. If two
 * genomes have the same health then the least "complex" of the two
 * will be deemed better.
 *
 * <h3>{@link genecode.Genome}</h3>
 *
 * <p>The genome holds a collection of {@link genecode.gene.Gene}s
 * which it uses to compute one or more values. The computation
 * happens for a given {@link genecode.Context}; external variables
 * may be accessed from that context.</p>
 *
 * <p>A genome may hold an arbitrary number of genes within it. In of
 * itself the genome doesn't really do much aside from keep all the
 * genes together and to give them an environment within which to
 * compute values. A single genome can compute multiple values using
 * the genes within it.</p>
 *
 * <p>The genes within the genome form a tree which is evaluated when
 * the genome computes its output values. (Strictly speaking this is
 * actually a graph, since it may be reentrant.)</p>
 *
 * <p>We don't model anything after specificity at this point. This is
 * because the way the code is used is already fairly specific.
 * I.e. the user will be presenting the Biome with a particular
 * problem and so is doing the specialisation step for it.</p>
 *
 * <p>Once created a genome should never change. Mutation of a genome
 * only happens at the creation stage; after that it should be
 * considered immutable. As such it is safe to retain {@link
 * genecode.Genome} instances. A {@link genecode.Genome} instance may
 * also be used as a key inside a {@link java.util.Map}.</p>
 *
 * <h3>{@link genecode.gene.Gene}</h3>
 *
 * <p>Each gene is responsible for computing a single value. By
 * combining these values the genome may compute its outputs. See the
 * {@link genecode.gene} documentation for more details.</p>
 *
 * <h2>Operation</h2>
 *
 * <p>A {@link genecode.Biome} instance is effectively a single
 * optimisation system. The process of generating new genome
 * generations, using a "health" score of each genome in the current
 * generation, is akin to hill-walking or simulated annealing.</p>
 *
 * <p>In order to drive the system the user should do the
 * following:</p><ul>
 *
 *  <li>Create a {@link genecode.Biome} instance.</li>
 *
 *  <li>Determine how the "health" of a genome may be computed; for
 *  example:<ul>
 *    
 *    <li>If you are simply trying to use the biome to approximate a
 *    mathematical function then you can evaluate each genome for
 *    various input values and see how well it corresponds to that
 *    function; the greater the error, the worse the health of the
 *    genome.</li>
 *
 *    <li>Alternatively, if the genome is driving some behaviour, one
 *    can evaluate the genome in various contexts and, if it is
 *    applicable to a context, see how well it performs. One can then
 *    create cumulative "score" for that genome to serve as its health.
 *
 *  </ul></li>
 *
 * <li>Create the next generation of genomes within the biome, using
 * the health values for each current genome.</li>
 *
 * <li>Repeat this process until it appears that the biome has
 * converged to a stable state.</li>
 *
 * </ul>
 *
 * <p>One should note that it is possible for a single biome to get
 * trapped in a local maximum. However, since you can evaluate
 * multiple biome instances in parallel, using multiple threads, it is
 * suggested that multiple biome instances be driven in
 * parallel. Then, as they each converge, the worse ones can be thrown
 * away and new ones created to explore other parts of the search
 * space.</p>
 *
 * <p>Whether new biomes should be seeded using genomes from existing
 * biomes is an open question. This is because when a biome has
 * converged, it is quite likely that one particular variant of a
 * single genome comprises most of its population. Letting genomes
 * from one biome into another one may simply mean that a single
 * successful genome takes over both instances. However, it is also
 * possible that two genomes from two biomes are different enough that
 * they will yield a better final result.</p>
 */
package genecode;
