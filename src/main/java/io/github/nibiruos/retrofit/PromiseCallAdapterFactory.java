package io.github.nibiruos.retrofit;

import com.google.common.base.Function;
import com.google.common.collect.Maps;

import org.nibiru.async.core.api.loop.Looper;
import org.nibiru.async.core.api.promise.Promise;

import java.lang.annotation.Annotation;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Map;

import javax.annotation.Nullable;

import retrofit2.CallAdapter;
import retrofit2.Retrofit;

import static com.google.common.base.Preconditions.checkNotNull;

public class PromiseCallAdapterFactory extends CallAdapter.Factory {
    public static PromiseCallAdapterFactory create(Looper looper) {
        checkNotNull(looper);
        return new PromiseCallAdapterFactory(looper);
    }

    private final Map<Class<? extends Exception>, Function<Throwable, ? extends Exception>> exceptionConverters;
    private final Looper looper;

    private PromiseCallAdapterFactory(Looper looper) {
        exceptionConverters = Maps.newHashMap();
        this.looper = looper;
    }

    public <E extends Exception> PromiseCallAdapterFactory convertException(Class<E> returnedException,
                                                                            Function<Throwable, E> exceptionConverter) {
        exceptionConverters.put(returnedException, exceptionConverter);
        return this;
    }

    @Nullable
    @Override
    public CallAdapter<?, ?> get(Type returnType,
                                 @Nullable Annotation[] annotations,
                                 @Nullable Retrofit retrofit) {
        checkNotNull(returnType);
        if (returnType instanceof ParameterizedType) {
            ParameterizedType parameterizedType = (ParameterizedType) returnType;
            return parameterizedType.getRawType() == Promise.class
                    ? new PromiseCallAdapter<>(parameterizedType.getActualTypeArguments()[0],
                    exceptionConverters.get(parameterizedType.getActualTypeArguments()[1]),
                    looper)
                    : null;
        } else {
            return null;
        }
    }
}