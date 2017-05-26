/**
 * <p>The genes which make up the genecode system.</p>
 *
 * <p>A collection of gene instances live inside a single {@link
 * genecode.Genome} instance. It is possible for one gene to refer to
 * another gene within that genome. A gene has one job, and that is to
 * compute a value; this is computed using its host genome and a given
 * {@link genecode.Context} instance.</p>
 *
 * <p>Different genes may compute values in different ways. For
 * example, a {@link genecode.gene.ConstantDouble} gene will always
 * return the same double value, a {@link genecode.gene.FunctionGene}
 * uses the values of other genes to compute a derived value, and an
 * {@link genecode.gene.Accessor} gene returns a value from a {@link
 * genecode.Context}.</p>
 *
 * <p>The {@link genecode.gene.FunctionGene} wraps functions from the
 * {@link genecode.function} package, See it for more details.</p>
 *
 * <p>Each gene instance has an associated {@link
 * genecode.gene.Gene.Handle}; this handle remains the same when that
 * gene is cloned, even though there is now a second copy of the gene
 * itself. As such, the gene for a single handle may be copied into a
 * new genome, get mutated, and later be copied back into another
 * child of the original genome (overwriting that child's copy) via
 * recombination.</p>
 *
 * <p>A single gene instance, like a {@link
 * genecode.gene.FunctionGene}, does not directly refer to another
 * instance but instead uses the handle to look up the required gene
 * from the genome. As such, it is possible for one gene to refer to
 * another one which is no longer in the genome, thus rendering it
 * unable to compute. Any gene which can't compute will yield a
 * "missing" value ({@link java.lang.Double#NaN}). It is also possible
 * for one gene to refer to another, which refers to another, and so
 * on until a gene refers back to the starting one. That is to say,
 * the evaluation tree made up from the genes in the genome may have
 * cycles in it; any cycles are also unable to compute and thus yield
 * missing values.</p>
 *
 * <p>With each reproductive cycle genes can potentially mutate. This
 * means different things for different gene implementations. For
 * example, a {@link genecode.gene.DoubleValue} gene might change its
 * value slightly, or a {@link genecode.gene.FunctionGene} might
 * change what other genes it refers to. This is how the evolutionary
 * process works in the system.</p>
 */
package genecode.gene;
