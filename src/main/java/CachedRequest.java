import io.reactivex.Observable;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

public class CachedRequest<T> {

    private final AtomicBoolean expired = new AtomicBoolean(true);
    private final Observable<T> source;
    private final long cacheExpirationInterval;
    private final TimeUnit cacheExpirationUnit;
    private Observable<T> current;

    public CachedRequest(Observable<T> o, long cacheExpirationInterval,
                         TimeUnit cacheExpirationUnit) {
        source = o;
        current = o;
        this.cacheExpirationInterval = cacheExpirationInterval;
        this.cacheExpirationUnit = cacheExpirationUnit;
    }

    public Observable<T> getCachedObservable() {
        return Observable.defer(() -> {
            if (expired.compareAndSet(true, false)) {
                current = source.cache();
                Observable.timer(cacheExpirationInterval, cacheExpirationUnit)
                        .subscribe(aLong -> expired.set(true));
            }
            return current;
        });
    }
}