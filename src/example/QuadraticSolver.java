package example;

import genecode.Biome;
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
import java.util.List;

/**
 * Attempt to find a quadratic equation using the solver.
 */
public class QuadraticSolver
{
    /**
     * Our function.
     */
    private static class Equation
        extends Function
    {
        public Equation()
        {
            super(Arrays.asList(Double.class), Double.class);
        }

        @Override
        protected Object safeCall(final Object[] args)
        {
            if (args == null || args.length != 1) {
                return null;
            }
            final Object value = args[0];
            if (!(value instanceof Double)) {
                return null;
            }

            final double x = (Double)value;
            return 3 * x * x - 5 * x + 7;
        }
    }

    /**
     * Get the most accurate genomes from the solver's biomes, if any.
     */
    private static List<Genome> getBest(final Solver solver)
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
    public static void main(String... args)
    {
        final Double[] values = new Double[2000];
        for (int i=0; i < values.length; i++) {
            values[i] = (20.0 * i / values.length) - 10.0;
        }
        final Variable<Double> v =
            new Variable<>(new Identifier<>("x", Double.class), values);

        final Solver solver =
            new Solver(
                Collections.singletonList(v),
                new Equation(),
                new SolverContext(),
                0.5,
                1.0,
                GeneFactory.SUPPLIERS,
                1,
                10000
            );

        int count = 0;
        while (true) {
            // Another tick of the clock...
            count++;
            solver.step(3, 0.01);

            // Find our current best ones
            final List<Genome> best = getBest(solver);

            // Talk to the world?
            System.out.println();
            for (Genome genome : best) {
                System.out.println(count + " " +
                                   solver.healthOf(genome) + " " +
                                   solver.coverage(genome) + " " +
                                   genome);
            }
        }
    }
}
