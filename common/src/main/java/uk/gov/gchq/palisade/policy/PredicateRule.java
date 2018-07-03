/*
 * Copyright 2018 Crown Copyright
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package uk.gov.gchq.palisade.policy;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import uk.gov.gchq.palisade.Justification;
import uk.gov.gchq.palisade.ToStringBuilder;
import uk.gov.gchq.palisade.User;

import java.util.function.Predicate;

import static java.util.Objects.requireNonNull;

/**
 * A {@link PredicateRule} is helper implementation of {@link Rule}. It is useful
 * when you need to set simple rules that don't require the {@link User} or {@link Justification}.
 *
 * @param <T> The type of the record. In normal cases the raw data will be deserialised
 *            by the record reader before being passed to the {@link Rule#apply(Object, User, Justification)}.
 */
@JsonPropertyOrder(alphabetic = true)
public class PredicateRule<T> implements Rule<T> {
    private final Predicate<T> predicate;

    /**
     * Constructs a {@link PredicateRule} with a given simple predicate rule to apply.
     * Note - using this means your rule will not be given the User or Justification.
     *
     * @param predicate the simple {@link Predicate} rule to wrap.
     */
    @JsonCreator
    public PredicateRule(@JsonProperty("predicate") final Predicate<T> predicate) {
        requireNonNull(predicate, "WrappedRule was initialised with a null predicate");
        this.predicate = predicate;
    }

    @Override
    public T apply(final T obj, final User user, final Justification justification) {
        return predicate.test(obj) ? obj : null;
    }

    @JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY, property = "class")
    public Predicate<T> getPredicate() {
        return predicate;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }

        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        final PredicateRule<?> that = (PredicateRule<?>) o;

        return new EqualsBuilder()
                .append(predicate, that.predicate)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(11, 37)
                .append(predicate)
                .toHashCode();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("predicate", predicate)
                .toString();
    }
}
