package genecode.function;

import java.util.Arrays;

/**
 * A function which does a compare of two types.
 */
public abstract class Compare
    extends Function
{
    private static final long serialVersionUID = 8732652567567523L;

    /**
     * Less than.
     */
    public static class LT
        extends Compare
    {
        /**
         * CTOR.
         *
         * @param type The subclass of {@link Comparable} which we take.
         */
        public LT(final Class<? extends Comparable> type)
        {
            super(Op.lt, type);
        }
    }

    /**
     * Less than or equal to.
     */
    public static class LTE
        extends Compare
    {
        /**
         * CTOR.
         *
         * @param type The subclass of {@link Comparable} which we take.
         */
        public LTE(final Class<? extends Comparable> type)
        {
            super(Op.lte, type);
        }
    }

    /**
     * Equal to.
     */
    public static class EQ
        extends Compare
    {
        /**
         * CTOR.
         *
         * @param type The subclass of {@link Comparable} which we take.
         */
        public EQ(final Class<? extends Comparable> type)
        {
            super(Op.eq, type);
        }
    }

    /**
     * Greater than or equal to.
     */
    public static class GTE
        extends Compare
    {
        /**
         * CTOR.
         *
         * @param type The subclass of {@link Comparable} which we take.
         */
        public GTE(final Class<? extends Comparable> type)
        {
            super(Op.gte, type);
        }
    }

    /**
     * Greater than.
     */
    public static class GT
        extends Compare
    {
        /**
         * CTOR.
         *
         * @param type The subclass of {@link Comparable} which we take.
         */
        public GT(final Class<? extends Comparable> type)
        {
            super(Op.gt, type);
        }
    }

    /**
     * The types of comparison.
     */
    private static enum Op {
        lt, lte, eq, gte, gt
    }

    // ----------------------------------------------------------------------

    /**
     * Our operation.
     */
    private final Op myOp;

    /**
     * CTOR.
     *
     * @param op   The compare operation we are performing.
     * @param type The subclass of {@link Comparable} which we take.
     */
    protected Compare(final Op op,
                      final Class<? extends Comparable> type)
    {
        super(Arrays.asList(type, type), Boolean.class);
        myOp = op;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @SuppressWarnings("unchecked")
    protected Object safeCall(final Object[] args)
    {
        final Object value0 = args[0];
        final Object value1 = args[1];
        if (!(value0 instanceof Comparable) ||
            !(value1 instanceof Comparable) ||
            !value0.getClass().equals(value1.getClass()))
        {
            return null;
        }

        final Comparable comparable0 = (Comparable)value0;
        final Comparable comparable1 = (Comparable)value1;
        final int cmp = comparable0.compareTo(comparable1);
        switch (myOp) {
        case lt:  return Boolean.valueOf(cmp <  0);
        case lte: return Boolean.valueOf(cmp <= 0);
        case eq:  return Boolean.valueOf(cmp == 0);
        case gte: return Boolean.valueOf(cmp >= 0);
        case gt:  return Boolean.valueOf(cmp >  0);
        default:  return null;
        }
    }
}
