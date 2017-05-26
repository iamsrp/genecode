package genecode.function;

import java.io.Serializable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A function. It takes an arbitrary number of inputs and yields an
 * output.
 *
 * <p>Generally speaking, all implementations should be stateless.
 */
public abstract class Function
    implements Cloneable,
               Serializable
{
    /**
     * Our logger.
     */
    public static final Logger LOG = Logger.getLogger(Function.class.getName());

    // ----------------------------------------------------------------------

    /**
     * Our argument types.
     */
    private final List<Class<?>> myArgTypes;

    /**
     * The type that we return.
     */
    private final Class<?> myReturnType;

    // ----------------------------------------------------------------------

    /**
     * CTOR.
     *
     * @param argTypes   The types of the arguments that this function takes.
     * @param returnType The type of the value which this function returns.
     *
     * @throws IllegalArgumentException If any of the types were {@code null}.
     */
    protected Function(final List<Class<?>> argTypes,
                       final Class<?>       returnType)
        throws IllegalArgumentException
    {
        if (returnType == null) {
            throw new IllegalArgumentException("Null returnType given");
        }
        if (argTypes != null && !argTypes.isEmpty()) {
            for (int i=0; i < argTypes.size(); i++) {
                if (argTypes.get(i) == null) {
                    throw new IllegalArgumentException(
                        "Null argType[" + i + "] given"
                    );
                }
            }
        }
        myReturnType = returnType;
        myArgTypes   = (argTypes == null)
            ? Collections.emptyList()
            : Collections.unmodifiableList(new ArrayList<>(argTypes));
    }

    /**
     * Get the number of arguments this function takes.
     *
     * @return The number of arguments.
     */
    public List<Class<?>> getArgTypes()
    {
        return myArgTypes;
    }

    /**
     * Get the return type of this function.
     *
     * @return The type of the value which the function returns.
     */
    public Class<?> getReturnType()
    {
        return myReturnType;
    }

    /**
     * Evaluate the function.
     *
     * @param args The array of arguments.
     *
     * @return {@code null} if the function could not be evaluated.
     *
     * @throws IllegalArgumentException If an error occurred during evaluation.
     */
    public final Object call(final Object... args)
    {
        if (args == null && !myArgTypes.isEmpty()) {
            throw new IllegalArgumentException(
                "Null arg list passed in"
            );
        }
        else if (args.length != myArgTypes.size()) {
            throw new IllegalArgumentException(
                "Wrong number of arguments used, " +
                "expected " + myArgTypes.size() + " " +
                "but had " + args.length + " for " + this
            );
        }
        for (int i=0; i < args.length; i++) {
            final Object arg = args[i];
            if (arg == null) {
                return null;
            }
            if (!myArgTypes.get(i).isAssignableFrom(arg.getClass())) {
                throw new IllegalArgumentException(
                    "Bad argument #" + i + ", " +
                    "expected a " + myArgTypes.get(i) + " " +
                    "but had a " + arg.getClass() + " for " + this
                );
            }
        }

        // Okay to call now
        try {
            return safeCall(args);
        }
        catch (Throwable t) {
            throw new IllegalArgumentException(
                "Failed to call " + this + "(" + Arrays.toString(args) + ")",
                t
            );
        }
    }

    /**
     * A detailed version of the function.
     *
     * @return A more detailed description of the function.
     */
    public String describe()
    {
        final StringBuilder sb = new StringBuilder();
        sb.append(getReturnType().getSimpleName()).append(' ');
        sb.append(getClass().getSimpleName());
        sb.append('(');
        for (int i=0; i < myArgTypes.size(); i++) {
            if (i > 0) {
                sb.append(", ");
            }
            sb.append(myArgTypes.get(i).getSimpleName());
        }
        sb.append(')');

        return sb.toString();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString()
    {
        return getClass().getSimpleName();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(final Object o)
    {
        if (!(o instanceof Function)) {
            return false;
        }

        final Function that = (Function)o;
        if (!that.getClass().equals(getClass())) {
            return false;
        }
        if (!that.myArgTypes.equals(myArgTypes)) {
            return false;
        }
        if (!that.myReturnType.equals(myReturnType)) {
            return false;
        }

        return true;
    }

    /**
     * Create a duplicate of this instance.
     *
     * @return The duplicate.
     */
    public Function clone()
    {
        try {
            return (Function)super.clone();
        }
        catch (CloneNotSupportedException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Actually call the function in a safe context.
     *
     * @param args The arguments to the function.
     *
     * @return The value which the function computes, or {@code null}
     *         if it failed to compute for some reason.
     */
    protected abstract Object safeCall(final Object[] args);
}
