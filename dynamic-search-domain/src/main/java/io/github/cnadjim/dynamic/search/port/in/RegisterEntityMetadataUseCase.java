package io.github.cnadjim.dynamic.search.port.in;

public interface RegisterEntityMetadataUseCase<T> {
    void register(Class<T> entityClass);
}
