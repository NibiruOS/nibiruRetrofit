package io.github.nibiruos.retrofit;

import com.google.common.base.Function;

import org.nibiru.async.core.api.loop.Looper;
import org.nibiru.async.core.api.promise.Deferred;
import org.nibiru.async.core.api.promise.Promise;

import java.lang.reflect.Type;

import retrofit2.Call;
import retrofit2.CallAdapter;
import retrofit2.Callback;
import retrofit2.Response;

import static com.google.common.base.Preconditions.checkNotNull;

public class PromiseCallAdapter<V, E extends Exception> implements CallAdapter<V, Promise<V, E>> {
    private final Type type;
    private final Function<Throwable, E> exceptionConverter;
    private final Looper looper;

    public PromiseCallAdapter(Type type,
                              Function<Throwable, E> exceptionConverter,
                              Looper looper) {
        this.type = checkNotNull(type);
        this.exceptionConverter = checkNotNull(exceptionConverter);
        this.looper = checkNotNull(looper);
    }

    @Override
    public Type responseType() {
        return type;
    }

    @Override
    public Promise<V, E> adapt(Call<V> call) {
        Deferred<V, E> deferred = Deferred.defer();
        call.enqueue(new Callback<V>() {
            @Override
            public void onResponse(Call<V> call, Response<V> response) {
                looper.post(() -> deferred.resolve(response.body()));
            }

            @Override
            public void onFailure(Call<V> call, Throwable throwable) {
                looper.post(() -> deferred.reject(exceptionConverter.apply(throwable)));
            }
        });
        return deferred.promise();
    }
}
