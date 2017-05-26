package genecode.gene;

import genecode.Context;
import genecode.Genome;
import genecode.function.Function;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * A gene which wraps a function call directly.
 */
public class FunctionGene
    extends AbstractGene
{
    // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -

    /**
     * Our function.
     */
    private Function myFunction;

    /**
     * The genes which we refer to as arguments, by their handles.
     */
    private List<Gene.Handle> myArgs;

    /**
     * The genes which we refer to as arguments, cached.
     */
    private Gene[] myArgGenes;

    /**
     * The genome associated with the cache.
     */
    private Genome myArgGenesGenome;

    /**
     * A place to hold the values which we pass to the function.
     */
    private Object[] myValues;

    // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -

    /**
     * CTOR.
     *
     * @param function The function which we wrap.
     */
    public FunctionGene(final Function function)
    {
        super(function.getReturnType());

        myFunction       = function;
        myArgs           = new ArrayList<>(function.getArgTypes().size());
        myArgGenes       = null;
        myArgGenesGenome = null;
        myValues         = new Object[function.getArgTypes().size()];
    }

    /**
     * Set the arguments. We use an array since order may be important.
     *
     * <p>Generally speaking you should not call this method. It's
     * mainly here to facilitate testing.
     *
     * @param args The arguments to set for this gene.
     *
     * @throws IllegalArgumentException If the args violate the gene's
     *                                  constraints.
     */
    public void setArgs(final Gene.Handle... args)
        throws IllegalArgumentException
    {
        if (args == null) {
            throw new IllegalArgumentException(
                "Given a null list of args"
            );
        }
        final List<Class<?>> argTypes = myFunction.getArgTypes();
        if (args.length != argTypes.size()) {
            throw new IllegalArgumentException(
                "Number of arguments, " + args.length + ", " +
                "did not match the expected number, " + argTypes.size()
            );
        }
        for (Gene.Handle arg : args) {
            if (arg == null) {
                throw new IllegalArgumentException(
                    "Args had a null value: " + args
                );
            }
        }

        // Safe to do
        flush();
        myArgs.clear();
        myArgs.addAll(Arrays.asList(args));
        myArgGenes       = null;
        myArgGenesGenome = null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getGraphSize(final Genome genome)
    {
        // Looped?
        if (myInGetGraphSize) {
            // If we just became re-entrant then there is nothing more
            // to see here. We return 1 since, though we have counted
            // ourselves already, we want to account for the fact that
            // we are a node that's referred to by another function.
            // This is a bit of a cheat but sort of makes semantic
            // sense.
            return 1;
        }

        myInGetGraphSize = true;
        try {
            int size = super.getGraphSize(genome);
            for (int i=0; i < myArgs.size(); i++) {
                final Gene gene = getArg(i, genome);
                size += (gene == null) ? 1 : gene.getGraphSize(genome);
            }
            return size;
        }
        finally {
            myInGetGraphSize = false;
        }
    }
    private boolean myInGetGraphSize = false;

    /**
     * {@inheritDoc}
     */
    @Override    
    public void getGraphHandles(final Genome            genome,
                                final List<Gene.Handle> dest)
    {
        // Looped?
        if (myInGetGraphHandles) {
            // Like getGraphSize() we handle reentrancy by adding this node
            dest.add(getHandle());
            return;
        }

        myInGetGraphHandles = true;
        try {
            super.getGraphHandles(genome, dest);
            for (int i=0; i < myArgs.size(); i++) {
                final Gene gene = getArg(i, genome);
                if (gene == null) {
                    dest.add(myArgs.get(i));
                }
                else {
                    gene.getGraphHandles(genome, dest);
                }
            }
        }
        finally {
            myInGetGraphHandles = false;
        }
    }
    private boolean myInGetGraphHandles = false;

    /**
     * {@inheritDoc}
     */
    @Override
    public void mutate(final Genome genome,
                       final double factor)
    {
        // Possibly change an input
        if (!myArgs.isEmpty() && Math.random() < factor) {
            // Pick one an change it
            final int index = (int)(Math.random() * myArgs.size());
            myArgs.set(
                index,
                genome.pickAnyHandle(
                    myFunction.getArgTypes().get(index)
                )
            );
        }
    }

    /**
     * {@inheritDoc}
     *
     * <p>Missing genes will be represented as {@code &lt;?&gt;}.
     */
    @Override
    public String toString(final Genome genome)
    {
        final StringBuilder sb = new StringBuilder();
        sb.append(myFunction).append("(");

        if (myInToString) {
            sb.append("<LOOPS>");
        }
        else {
            myInToString = true;
            try {
                for (int i=0; i < myArgs.size(); i++) {
                    if (i > 0) {
                        sb.append(",");
                    }

                    final Gene gene = getArg(i, genome);
                    if (gene != null) {
                        sb.append(gene.toString(genome));
                    }
                    else {
                        sb.append("<?>");
                    }
                }
            }
            finally {
                myInToString = false;
            }
        }

        sb.append(')');
        return sb.toString();
    }
    private boolean myInToString = false;

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(final Object o)
    {
        if (!super.equals(o)) {
            return false;
        }

        if (!(o instanceof FunctionGene)) {
            return false;
        }

        final FunctionGene that = (FunctionGene)o;
        if (!that.myFunction.equals(myFunction)) {
            return false;
        }
        if (!that.myArgs.equals(myArgs)) {
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
        final FunctionGene gene = (FunctionGene)super.clone();
        gene.myFunction       = myFunction.clone();
        gene.myArgs           = new ArrayList<>(gene.myArgs);
        gene.myArgGenes       = null;
        gene.myArgGenesGenome = null;
        gene.myValues         = new Object[myValues.length];
        return gene;
    }

    /**
     * {@inheritDoc}
     *
     * <p>We accumulate the minimum number of arguments.
     */
    @Override
    protected void safeInit(final Genome genome)
        throws IllegalStateException
    {
        for (int i=0; i < myFunction.getArgTypes().size(); i++) {
            myArgs.add(
                genome.pickAnyHandle(myFunction.getArgTypes().get(i))
            );
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Object safeEvaluate(final Context context,
                                  final Genome  genome)
    {
        // Populate the arguments for the functioon
        for (int i=0; i < myValues.length; i++) {
            final Gene gene = getArg(i, genome);
            if (gene == null) {
                return null;
            }
            myValues[i] = gene.evaluate(context, genome);
            if (myValues[i] == null) {
                return null;
            }
        }

        // Hand off
        return myFunction.call(myValues);
    }

    /**
     * Get the gene argument with the given index from the genome, if
     * it exists.
     *
     * @param index  The index of the argument to get.
     * @param genome The genome to get the argument from.
     *
     * @return The gene or {@code null} if it was not found.
     */
    private Gene getArg(final int    index,
                        final Genome genome)
    {
        if (index < 0 || index >= myArgs.size()) {
            return null;
        }
        else {
            // (Re)build the cache?
            if (myArgGenesGenome != genome) { // pointer compare
                myArgGenes = null;
            }
            if (myArgGenes == null) {
                myArgGenes = new Gene[myArgs.size()];
                for (int i=0; i < myArgs.size(); i++) {
                    myArgGenes[i] = genome.get(myArgs.get(i));
                }
                myArgGenesGenome = genome;
            }

            // Grab from the cache
            return myArgGenes[index];
        }
    }
}
