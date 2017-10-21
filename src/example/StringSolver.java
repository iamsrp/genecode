package example;

import genecode.Biome;
import genecode.Context;
import genecode.Context.Identifier;
import genecode.Genome;
import genecode.Solver;
import genecode.Solver.SolverContext;
import genecode.Solver.Variable;
import genecode.function.Function;
import genecode.gene.GeneFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Attempt to find a mapping between pairs of strings.
 */
public class StringSolver
{
    /**
     * Our function.
     */
    private static class Mapping
        extends Function
    {
        private final Map<String,String> myMappings;

        public Mapping(final Map<String,String> mappings)
        {
            super(Arrays.asList(String.class), String.class);

            myMappings = new HashMap<>(mappings);
        }

        public String[] values()
        {
            return myMappings.keySet().toArray(new String[myMappings.size()]);
        }

        @Override
        protected Object safeCall(final Object[] args)
        {
            if (args == null || args.length != 1) {
                return null;
            }
            final Object value = args[0];
            if (!(value instanceof String)) {
                return null;
            }
            else {
                return myMappings.get((String)value);
            }
        }
    }

    /**
     * Get the most accurate genomes from the solver's biomes, if any.
     */
    private List<Genome> getBest(final Solver solver)
    {
        final List<Genome> all = new ArrayList<>();
        for (Biome biome : solver.getBiomes()) {
            all.addAll(biome.getGenomes());
        }

        Collections.sort(
            all,
            (a, b) -> -Double.compare(solver.healthOf(a), solver.healthOf(b))
        );

        return all.subList(0, 10);
    }

    /**
     * Entry point.
     */
    protected void solve(final Map<String,String> mappings)
    {
        final Mapping mapping = new Mapping(mappings);
        final String[] values = mapping.values();
        final Identifier<String> id = new Identifier<>("name", String.class);
        final Variable<String> v = new Variable<>(id, values);

        final Solver solver =
            new Solver(
                Collections.singletonList(v),
                mapping,
                new SolverContext(),
                0.75,
                0.00,
                GeneFactory.SUPPLIERS,
                1,
                25000
            );

        int count = 0;
        while (true) {
            // Another tick of the clock...
            count++;

            // How noisy to be as we count. We use a sine wave to
            // drift in and out of noisiness.
            final double noise =
                0.1 + (0.1 * Math.sin(count / 180.0 / 10.0 * Math.PI));

            // And step the solver onwards
            solver.step(2, noise);

            // Find our current best ones
            final List<Genome> best = getBest(solver);

            // Talk to the world?
            System.out.println();
            Genome top = null;
            for (Genome genome : best) {
                System.out.println(
                    count + " " +
                    String.format("%.02f", noise                  ) + " " +
                    String.format("%.02f", solver.healthOf(genome)) + " " +
                    String.format("%.02f", solver.coverage(genome)) + " " +
                    genome
                );
                if (top == null) {
                    top = genome;
                }
            }

            // Print out the results of the current top one
            if (top != null) {
                for (int i=0; i < values.length; i++) {
                    if (i >= 50) {
                        System.out.println("  ...");
                        break;
                    }
                    final String string = values[i];
                    final Context context =
                        new Context() {
                            @Override
                            public long getId()
                            {
                                return string.hashCode();
                            }

                            @Override
                            public Object access(final Identifier<?> ident) {
                                return (id.equals(ident)) ? string : null;
                            }
                        };

                    final Object obj = top.evaluate(context, 0);
                    final String str =
                        (obj == null) ? "null" : ("\"" + obj + "\"");
                    System.out.println("  \"" + string + "\" -> " + str);
                }

                // Are we done?
                if (Math.abs(noise) < 1e-10 &&
                    Math.abs(1.0 - solver.healthOf(top)) < 1e-10)
                {
                    System.exit(0);
                }
            }
        }
    }
}
