package org.fao.fenix.fmd.tools.utils;

import java.lang.ref.SoftReference;
import java.util.*;

public class SoftMap<K,V> implements Map<K,V> {
    private Map<K,SoftReference<V>> baseMap = new HashMap<K, SoftReference<V>>();

    @Override
    public V get(Object key) {
        SoftReference<V> value = baseMap.get(key);
        return value!=null ? value.get() : null;
    }

    @Override
    public V put(K key, V value) {
        SoftReference<V> previousValue = baseMap.put(key, new SoftReference(value));
        return previousValue!=null ? previousValue.get() : null;
    }

    @Override
    public V remove(Object key) {
        SoftReference<V> value = baseMap.remove(key);
        return value!=null ? value.get() : null;
    }

    @Override
    public void putAll(Map<? extends K, ? extends V> m) {
        if (m!=null)
            for (Entry<? extends K, ? extends V> entry : m.entrySet())
                put(entry.getKey(),entry.getValue());
    }

    @Override
    public Collection<V> values() {
        Collection<V> values = new LinkedList<V>();
        for (SoftReference<V> v : baseMap.values())
            values.add(v!=null ? v.get() : null);
        return values;
    }

    @Override
    public Set<Entry<K, V>> entrySet() {
        Set<Entry<K, V>> entrySet = new HashSet<Entry<K, V>>();
        for (Entry<K, SoftReference<V>> entry : baseMap.entrySet()) {
            SoftReference<V> v = entry.getValue();
            entrySet.add(new AbstractMap.SimpleEntry<K, V>(entry.getKey(), v!=null ? v.get() : null));
        }
        return entrySet;
    }








    @Override
    public int size() {
        return baseMap.size();
    }

    @Override
    public boolean isEmpty() {
        return baseMap.isEmpty();
    }

    @Override
    public boolean containsKey(Object key) {
        return get(key)!=null;
    }

    @Override
    public boolean containsValue(Object value) {
        return baseMap.containsValue(new SoftReference<V>((V)value));
    }

    @Override
    public void clear() {
        baseMap.clear();
    }

    @Override
    public Set<K> keySet() {
        return baseMap.keySet();
    }
}
