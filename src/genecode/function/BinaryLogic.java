package genecode.function;

import java.util.Arrays;

/**
 * A function which performs a binary logic opertion.
 */
public abstract class BinaryLogic
    extends Function
{
    /**
     * Logical AND.
     */
    public static class And
        extends BinaryLogic
    {
        public And()
        {
            super(Op.AND);
        }
    }

    /**
     * Logical OR.
     */
    public static class Or
        extends BinaryLogic
    {
        public Or()
        {
            super(Op.OR);
        }
    }

    /**
     * Logical XOR.
     */
    public static class Xor
        extends BinaryLogic
    {
        public Xor()
        {
            super(Op.XOR);
        }
    }

    // -------------------------------------------------------------------------

    private static final long serialVersionUID = 287874623487675L;

    /**
     * Which logic operation.
     */
    private static enum Op
    {
        AND, OR, XOR;
    }

    /**
     * Which operation.
     */
    private final Op myOp;

    /**
     * CTOR.
     *
     * @param op  The compare operation we are performing.
     */
    protected BinaryLogic(final Op op)
    {
        super(Arrays.asList(Boolean.class, Boolean.class), Boolean.class);
        myOp = op;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Object safeCall(final Object[] args)
    {
        final Object value0 = args[0];
        final Object value1 = args[1];
        if (!(value0 instanceof Boolean) || !(value1 instanceof Boolean)) {
            return null;
        }

        final boolean bool0 = (Boolean)value0;
        final boolean bool1 = (Boolean)value1;

        switch (myOp) {
        case AND: return Boolean.valueOf(bool0 && bool1);
        case OR:  return Boolean.valueOf(bool0 || bool1);
        case XOR: return Boolean.valueOf(bool0 ^  bool1);
        default:  return null;
        }
    }
}
