package genecode.function;

import java.lang.reflect.Array;

import java.util.ArrayList;
import java.util.List;

/**
 * A function which create an array containing the arguments as
 * elements.
 */
public class ArrayOf
    extends Function
{
    private static final long serialVersionUID = 64387625358976253L;

    /**
     * Generate a list of the same value, repeated.
     *
     * @param argType  The type to repeat.
     * @param count    The number of times to repeat it.
     */
    private static List<Class<?>> repeat(final Class<?> argType,
                                         final int      count)
    {
        final List<Class<?>> result = new ArrayList<>(count);
        for (int i=0; i < count; i++) {
            result.add(argType);
        }
        return result;
    }

    /**
     * CTOR.
     *
     * @param argType  The type of the array being wrapped.
     * @param count    The number of arguments to take for wrapping.
     */
    public ArrayOf(final Class<?> argType, final int count)
    {
        super(repeat(argType, count),
              Array.newInstance(argType, 0).getClass());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString()
    {
        return super.toString() + "<" + getReturnType().getSimpleName() + ">";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Object safeCall(final Object[] args)
    {
        // Create the result and put in the arguments as values
        final Object result =
            Array.newInstance(getReturnType().getComponentType(),
                              args.length);
        for (int i=0; i < args.length; i++) {
            Array.set(result, i, args[i]);
        }
        return result;
    }
}
