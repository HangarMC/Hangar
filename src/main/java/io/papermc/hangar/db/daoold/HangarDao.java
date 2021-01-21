package io.papermc.hangar.db.daoold;

public class HangarDao<T> {

    private final T dao;

    public HangarDao(T dao) {
        this.dao = dao;
    }

    public T get() {
        return dao;
    }
}
