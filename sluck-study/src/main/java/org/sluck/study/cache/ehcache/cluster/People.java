package org.sluck.study.cache.ehcache.cluster;


import java.io.Serializable;

/**
 * Created by sunxy on 2016/4/21 20:37.
 */
public class People implements Serializable {
    private static final long serialVersionUID = 28327805506017106L;

    private String name;

    private People(){}

    public People(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        People people = (People) o;

        return name != null ? name.equals(people.name) : people.name == null;

    }

    @Override
    public int hashCode() {
        return name != null ? name.hashCode() : 0;
    }

    @Override
    public String toString() {
        return "People{" +
                "name='" + name + '\'' +
                '}';
    }
}
