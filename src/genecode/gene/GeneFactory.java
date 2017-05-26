package genecode.gene;

import java.util.Collection;
import java.util.function.Supplier;

/**
 * How we create random gene instances.
 */
@FunctionalInterface
public interface GeneFactory
{
    /**
     * The list of standard suppliers.
     */
    public static final Collection<Supplier<Gene>> SUPPLIERS =
        DefaultGeneFactory.getSuppliers();

    /**
     * Get a random gene instance.
     *
     * <p>The result of this method needs to be init()'d in a genome
     * before use.
     *
     * @return The generated gene.
     */
    public Gene generate();
}
