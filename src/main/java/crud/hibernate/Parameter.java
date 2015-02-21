/* Copyright 2015 Rick Warren
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package crud.hibernate;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;

import javax.annotation.Nonnull;

import org.hibernate.Query;
import org.hibernate.type.StandardBasicTypes;
import org.hibernate.type.Type;

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;


public abstract class Parameter {

    protected final Object value;
    protected final Optional<Type> type;


    public static Builder withValue(@Nonnull final Object value) {
        return new Builder(value);
    }

    public static PluralBuilder withValues(final Collection<?> value) {
        return new PluralBuilder(value);
    }

    public static EntityBuilder withEntity(@Nonnull final Object entity) {
        return new EntityBuilder(entity);
    }

    /**
     * Convenience method that preassigns the {@link Type}.
     */
    public static Builder withString(@Nonnull final String value) {
        return new Builder(value).ofType(StandardBasicTypes.STRING);
    }

    /**
     * Convenience method that preassigns the {@link Type}.
     */
    public static Builder withCharacter(final char value) {
        return new Builder(value).ofType(StandardBasicTypes.CHARACTER);
    }

    /**
     * Convenience method that preassigns the {@link Type}.
     */
    public static Builder withBoolean(final boolean value) {
        return new Builder(value).ofType(StandardBasicTypes.BOOLEAN);
    }

    /**
     * Convenience method that preassigns the {@link Type}.
     */
    public static Builder withByte(final byte value) {
        return new Builder(value).ofType(StandardBasicTypes.BYTE);
    }

    /**
     * Convenience method that preassigns the {@link Type}.
     */
    public static Builder withShort(final short value) {
        return new Builder(value).ofType(StandardBasicTypes.SHORT);
    }

    /**
     * Convenience method that preassigns the {@link Type}.
     */
    public static Builder withInteger(final int value) {
        return new Builder(value).ofType(StandardBasicTypes.INTEGER);
    }

    /**
     * Convenience method that preassigns the {@link Type}.
     */
    public static Builder withLong(final long value) {
        return new Builder(value).ofType(StandardBasicTypes.LONG);
    }

    /**
     * Convenience method that preassigns the {@link Type}.
     */
    public static Builder withFloat(final float value) {
        return new Builder(value).ofType(StandardBasicTypes.FLOAT);
    }

    /**
     * Convenience method that preassigns the {@link Type}.
     */
    public static Builder withDouble(final double value) {
        return new Builder(value).ofType(StandardBasicTypes.DOUBLE);
    }

    /**
     * Convenience method that preassigns the {@link Type}.
     */
    public static Builder withBinary(@Nonnull final byte[] value) {
        return new Builder(value).ofType(StandardBasicTypes.BINARY);
    }

    /**
     * Convenience method that preassigns the {@link Type}.
     */
    public static Builder withText(@Nonnull final String value) {
        return new Builder(value).ofType(StandardBasicTypes.TEXT);
    }

    /**
     * Convenience method that preassigns the {@link Type}.
     */
    public static Builder withSerializable(@Nonnull final Serializable value) {
        return new Builder(value).ofType(StandardBasicTypes.SERIALIZABLE);
    }

    /**
     * Convenience method that preassigns the {@link Type}.
     */
    public static Builder withLocale(@Nonnull final Locale value) {
        return new Builder(value).ofType(StandardBasicTypes.LOCALE);
    }

    /**
     * Convenience method that preassigns the {@link Type}.
     */
    public static Builder withBigDecimal(@Nonnull final BigDecimal value) {
        return new Builder(value).ofType(StandardBasicTypes.BIG_DECIMAL);
    }

    /**
     * Convenience method that preassigns the {@link Type}.
     */
    public static Builder withNigInteger(@Nonnull final BigInteger value) {
        return new Builder(value).ofType(StandardBasicTypes.BIG_INTEGER);
    }

    /**
     * Convenience method that preassigns the {@link Type}.
     */
    public static Builder withDate(@Nonnull final Date value) {
        return new Builder(value).ofType(StandardBasicTypes.DATE);
    }

    /**
     * Convenience method that preassigns the {@link Type}.
     */
    public static Builder withTime(@Nonnull final Date value) {
        return new Builder(value).ofType(StandardBasicTypes.TIME);
    }

    /**
     * Convenience method that preassigns the {@link Type}.
     */
    public static Builder withTimestamp(@Nonnull final Date value) {
        return new Builder(value).ofType(StandardBasicTypes.TIMESTAMP);
    }

    /**
     * Convenience method that preassigns the {@link Type}.
     */
    public static Builder withCalendar(@Nonnull final Calendar value) {
        return new Builder(value).ofType(StandardBasicTypes.CALENDAR);
    }

    /**
     * Convenience method that preassigns the {@link Type}.
     */
    public static Builder withCalendarDate(@Nonnull final Calendar value) {
        return new Builder(value).ofType(StandardBasicTypes.CALENDAR_DATE);
    }

    @Override
    public final String toString() {
        final StringBuilder buf = new StringBuilder();
        appendShortClassName(buf);
        buf.append('(');
        appendIdentifier(buf);
        buf.append(": ");
        appendValue(buf);
        appendType(buf);
        buf.append(')');
        return buf.toString();
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        // Don't check instanceof: subclass already did this
        final Parameter other = (Parameter) obj;
        if (!this.value.equals(other.value)) {
            return false;
        }
        if (!this.type.equals(other.type)) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + this.value.hashCode();
        result = prime * result + this.type.hashCode();
        return result;
    }

    /**
     * Set this parameter on the given {@link Query}.
     */
    protected abstract void apply(Query query);

    /**
     * Called by {@link #toString()}.
     */
    protected abstract void appendShortClassName(StringBuilder buf);

    /**
     * Called by {@link #toString()}.
     */
    protected abstract void appendIdentifier(StringBuilder buf);

    /**
     * Called by {@link #toString()}.
     */
    protected void appendValue(final StringBuilder buf) {
        // Don't call value.toString: might be huge and complicated
        buf.append(this.value.getClass().getName())
           .append('@')
           .append(System.identityHashCode(this.value));
    }

    /**
     * Called by {@link #toString()}.
     */
    protected void appendType(final StringBuilder buf) {
        if (this.type.isPresent()) {
            buf.append(" as ").append(this.type.get().getName());
        }
    }

    private Parameter(@Nonnull final Object value, final Optional<Type> type) {
        this.type = Objects.requireNonNull(type);
        this.value = Objects.requireNonNull(value);
    }


    public static class Builder {
        private final @Nonnull Object value;
        private Optional<Type> type = Optional.absent();

        private Builder(@Nonnull final Object value) {
            this.value = Objects.requireNonNull(value);
        }

        public Builder ofType(final Type theType) {
            this.type = Optional.of(theType);
            return this;
        }

        public Parameter atPosition(final int position) {
            return new AtPosition(position, this.value, this.type);
        }

        public Parameter byName(final String name) {
            return new ByName(name, this.value, this.type);
        }
    }


    public static class EntityBuilder {
        private final @Nonnull Object value;

        private EntityBuilder(@Nonnull final Object value) {
            this.value = Objects.requireNonNull(value);
        }

        public Parameter atPosition(final int position) {
            return new EntityAtPosition(position, this.value);
        }

        public Parameter byName(final String name) {
            return new EntityByName(name, this.value);
        }
    }


    public static class PluralBuilder {
        private final Collection<?> value;
        private Optional<Type> type = Optional.absent();

        private PluralBuilder(final Collection<?> value) {
            this.value = Objects.requireNonNull(value);
        }

        public PluralBuilder ofType(final Type theType) {
            this.type = Optional.of(theType);
            return this;
        }

        public Parameter byName(final String name) {
            return new PluralByName(name, this.value, this.type);
        }
    }


    private static class AtPosition extends Parameter {
        protected final int position;

        public AtPosition(final int position, final Object value, final Optional<Type> type) {
            super(value, type);
            this.position = position;
            Preconditions.checkArgument(this.position >= 0, "position out of range");
        }

        @Override
        public boolean equals(final Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final AtPosition other = (AtPosition) obj;
            if (this.position != other.position) {
                return false;
            }
            return super.equals(obj);
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + super.hashCode();
            result = prime * result + this.position;
            return result;
        }

        @Override
        protected void appendShortClassName(final StringBuilder buf) {
            buf.append("Parameter.AtPosition");
        }

        @Override
        protected void appendIdentifier(final StringBuilder buf) {
            buf.append(this.position);
        }

        @Override
        protected void apply(final Query query) {
            if (this.type.isPresent()) {
                query.setParameter(this.position, this.value, this.type.get());
            } else {
                query.setParameter(this.position, this.value);
            }
        }
    }


    private static class EntityAtPosition extends AtPosition {
        private EntityAtPosition(final int position, @Nonnull final Object entity) {
            super(position, entity, Optional.<Type>absent());
        }

        @Override
        protected void apply(final Query query) {
            query.setEntity(this.position, this.value);
        }

        @Override
        protected void appendType(final StringBuilder buf) {
            buf.append(" as entity");
        }
    }


    private static class ByName extends Parameter {
        protected final String name;

        public ByName(@Nonnull final String name, @Nonnull final Object value, final Optional<Type> type) {
            super(value, type);
            this.name = Objects.requireNonNull(name);
        }

        @Override
        public boolean equals(final Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final ByName other = (ByName) obj;
            if (!this.name.equals(other.name)) {
                return false;
            }
            return super.equals(obj);
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + super.hashCode();
            result = prime * result + this.name.hashCode();
            return result;
        }

        @Override
        protected void apply(final Query query) {
            if (this.type.isPresent()) {
                query.setParameter(this.name, this.value, this.type.get());
            } else {
                query.setParameter(this.name, this.value);
            }
        }

        @Override
        protected void appendShortClassName(final StringBuilder buf) {
            buf.append("Parameter.ByName");
        }

        @Override
        protected void appendIdentifier(final StringBuilder buf) {
            buf.append(this.name);
        }
    }


    private static class EntityByName extends ByName {
        private EntityByName(@Nonnull final String name, @Nonnull final Object entity) {
            super(name, entity, Optional.<Type>absent());
        }

        @Override
        protected void apply(final Query query) {
            query.setEntity(this.name, this.value);
        }

        @Override
        protected void appendType(final StringBuilder buf) {
            buf.append(" as entity");
        }
    }


    private static class PluralByName extends ByName {
        public PluralByName(final String name, final Collection<?> value, final Optional<Type> type) {
            super(name, value, type);
        }

        @Override
        protected void apply(final Query query) {
            if (this.type.isPresent()) {
                query.setParameterList(this.name, values(), this.type.get());
            } else {
                query.setParameterList(this.name, values());
            }
        }

        @Override
        protected void appendValue(final StringBuilder buf) {
            // Don't call Collection.toString: might be lots of them
            buf.append('<').append(values().size()).append(" values>");
        }

        private Collection<?> values() {
            return (Collection<?>) this.value;
        }
    }

}
