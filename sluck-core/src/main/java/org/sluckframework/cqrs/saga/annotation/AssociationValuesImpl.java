package org.sluckframework.cqrs.saga.annotation;

import org.sluckframework.cqrs.saga.AssociationValue;
import org.sluckframework.cqrs.saga.AssociationValues;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * 关联值集合的默认实现
 *
 * Author: sunxy
 * Created: 2015-09-14 18:29
 * Since: 1.0
 */
public class AssociationValuesImpl implements AssociationValues, Serializable {

    private static final long serialVersionUID = 6756483053106696964L;

    private Set<AssociationValue> values = new CopyOnWriteArraySet<>();
    private transient Set<AssociationValue> addedValues = new HashSet<>();
    private transient Set<AssociationValue> removedValues = new HashSet<>();

    @Override
    public int size() {
        return values.size();
    }

    @Override
    public boolean contains(AssociationValue associationValue) {
        return values.contains(associationValue);
    }

    @Override
    public Iterator<AssociationValue> iterator() {
        return Collections.unmodifiableSet(values).iterator();
    }

    @Override
    public boolean add(AssociationValue associationValue) {
        final boolean added = values.add(associationValue);
        if (added) {
            initializeChangeTrackers();
            if (!removedValues.remove(associationValue)) {
                addedValues.add(associationValue);
            }
        }
        return added;
    }

    @Override
    public boolean remove(AssociationValue associationValue) {
        final boolean removed = values.remove(associationValue);
        if (removed) {
            initializeChangeTrackers();
            if (!addedValues.remove(associationValue)) {
                removedValues.add(associationValue);
            }
        }
        return removed;
    }

    private void initializeChangeTrackers() {
        if (removedValues == null) {
            removedValues = new HashSet<>();
        }
        if (addedValues == null) {
            addedValues = new HashSet<>();
        }
    }

    @Override
    public Set<AssociationValue> asSet() {
        return Collections.unmodifiableSet(values);
    }

    @Override
    public Set<AssociationValue> removedAssociations() {
        if (removedValues == null || removedValues.isEmpty()) {
            return Collections.emptySet();
        }
        return removedValues;
    }

    @Override
    public Set<AssociationValue> addedAssociations() {
        if (addedValues == null || addedValues.isEmpty()) {
            return Collections.emptySet();
        }
        return addedValues;
    }

    @Override
    public void commit() {
        if (addedValues != null) {
            addedValues.clear();
        }
        if (removedValues != null) {
            removedValues.clear();
        }
    }

}