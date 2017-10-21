package genecode;

import java.io.Serializable;

import java.util.Objects;

/**
 * The context for {@link Genome} evaluation.
 */
public abstract class Context
{
    /**
     * An identifier for a value which may be accessed from a {@link
     * Context}.
     *
     * <p>Instances with the same name are considered to be the same
     * identifier.
     */
    public static final class Identifier<T>
        implements Serializable
    {
        private static final long serialVersionUID = 487873487358635675L;

        /**
         * The name of the identifier.
         */
        private final String myName;

        /**
         * The type of the value which the identifier references.
         */
        private final Class<T> myValueType;

        /**
         * CTOR.
         *
         * @param name      The name of this identifier for printing it.
         * @param valueType The type of the value which this identifier 
         *                  references.
         */
        public Identifier(final String name, final Class<T> valueType)
        {
            Objects.requireNonNull(name);
            Objects.requireNonNull(valueType);
            myName = name;
            myValueType = valueType;
        }

        /**
         * Get the type of the value for this identifier.
         */
        public Class<T> getValueType()
        {
            return myValueType;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public boolean equals(final Object object)
        {
            if (this == object) {
                return true;
            }
            if (!(object instanceof Identifier)) {
                return false;
            }
            final Identifier that = (Identifier)object;

            return (that.myName     .equals(myName) &&
                    that.myValueType.equals(myValueType));
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public int hashCode()
        {
            return myName.hashCode() ^ myValueType.hashCode();
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public String toString()
        {
            return myValueType.getSimpleName() + ":\"" + myName + '"';
        }
    }

    /**
     * Get the unique ID of this context. The ID of one context should
     * be different to that of another if any of the accessed values
     * differ.
     *
     * @return The context ID.
     */
    public abstract long getId();

    /**
     * Access the value for a given {@link Identifier} from the
     * context.
     *
     * @param id The identifier to get the value for.
     *
     * @return {@code Double.NaN} if no such value exists.
     */
    public Object access(final Identifier<?> id)
    {
        return null;
    }
}
