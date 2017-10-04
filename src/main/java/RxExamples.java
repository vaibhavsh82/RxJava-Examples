import com.sun.media.jfxmediaimpl.MediaDisposer;
import io.reactivex.Flowable;
import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;

import static java.lang.System.*;

public class RxExamples {

    private static Repository repository = new Repository();

    public static void main(String[] args) throws InterruptedException {

        Observable.just("Emit data").subscribe(out::println);

        futureEvents();

        flatMapExample();

        zipOperator();

        runInParallel();

        cacheAndNetwork();
    }


    private static void futureEvents() {
        repository.getUsersFirstName()
                .blockingSubscribe(out::println, throwable -> out.println(throwable.getMessage()));
        // Thread.sleep(5000);
    }


    private static void flatMapExample() {
        repository.getDiskData()
                .flatMap((Function<String, ObservableSource<?>>) s -> {
                    out.println(s);
                    return repository.getNetworkData();
                })
                .blockingSubscribe(out::println);
    }


    private static void zipOperator() {
        Observable.zip(repository.getUsersFirstName(),
                repository.getUsersLastName(), (source1, source2) -> source1 + " " + source2)
                .filter(s -> s.length() > 12)
                .blockingSubscribe(out::println);
    }

    /**
     * Run the an operation in parallel.
     */
    private static void runInParallel() {

        // Sequential
        Observable.range(1, 10)
                .map(v -> v * v)
                .subscribeOn(Schedulers.computation())
                .blockingSubscribe(out::println);


        // Parallel
        Observable.range(1, 10)
                .flatMap(v ->
                        Observable.just(v)
                                .subscribeOn(Schedulers.io())
                                .map(w -> w * w)
                )
                .blockingSubscribe(out::println);

        // Alternate way of running parallel
        Flowable.range(1, 10)
                .parallel()
                .runOn(Schedulers.computation())
                .map(v -> v * v)
                .sequential()
                .blockingSubscribe(out::println);
    }


    private static void cacheAndNetwork() {
        Observable.merge(
                repository.getDiskData(),
                repository.getNetworkData()
                        .doOnNext(result -> {
                            out.println("Do on nex:  " + result);
                            repository.saveDataToDisk(result);
                        })
                        .onErrorResumeNext(error -> {
                            repository.getDiskData();
                        }))
                .blockingSubscribe(out::println);

        // There is an issue in the above example. If the network response is being returned before disk data the network (fresh) response will be overwritten by
        // disk response which is stale. The below method fixes that.
        betterCacheAndNetwork();
    }


    private static void betterCacheAndNetwork() {

        /*
        // step 1: get the diskData until network data is not being returned
        repository.getDiskData()
                .takeUntil(repository.getNetworkData());

        // step 2 : Merge both the observable.
        Observable.merge(repository.getNetworkData(),
                repository.getDiskData()
                        .takeUntil(repository.getNetworkData()));

        */

        repository.getNetworkData()
                .publish(networkObservable ->
                        Observable.merge(networkObservable,
                                repository.getDiskData()
                                        .takeUntil(networkObservable)))
                .blockingSubscribe(s -> out.println("Get fresh data:" + s));
    }
}
