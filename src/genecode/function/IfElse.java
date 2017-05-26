package genecode.function;

import java.util.Arrays;

/**
 * A function which picks one of two other values based on the value
 * of a third: {@code (v0) ? v1 : v2}.
 */
public class IfElse
    extends Function
{
    private static final long serialVersionUID = 473763477634078378L;

    /**
     * CTOR.
     *
     * @param returnType The type which is returned.
     */
    public IfElse(final Class<?> returnType)
    {
        super(Arrays.asList(Boolean.class, returnType, returnType), returnType);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Object safeCall(final Object[] args)
    {
        if (args[0] instanceof Boolean) {
            return ((Boolean)args[0]) ? args[1] : args[2];
        }
        else {
            return null;
        }
    }
}
