import com.sun.media.jfxmediaimpl.MediaDisposer;
import io.reactivex.Flowable;
import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.BooleanSupplier;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.observables.ConnectableObservable;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.subjects.BehaviorSubject;

import java.io.Console;

import static java.lang.System.*;

public class RxExamples {

    private static Repository repository = new Repository();
    static BehaviorSubject<String> subject = BehaviorSubject.create();

    public static void main(String[] args) throws InterruptedException {

        // Observable.just("Emit data").subscribe(out::println);

        // futureEvents();

        flatMapExample();

        // zipOperator();

        // runInParallel();

        // cacheAndNetwork();

        // flatMapWithRetryExample()

        // publishAndConnect();
    }


    private static void futureEvents() {
        repository.getUsersFirstName()
                .blockingSubscribe(out::println, throwable -> out.println(throwable.getMessage()));
        // Thread.sleep(5000);
    }


    private static void flatMapExample() throws InterruptedException {

       Disposable disposable =  repository.getDiskData()
                .flatMap((Function<String, ObservableSource<String>>) s -> {
                    out.println(s);
                    return repository.getNetworkData();
                })
               .subscribe(out::println);


       Thread.sleep(3000);
    }

    /*private void checkBehavior() {
        subject.onNext(repository.getDiskData().);
    }*/


    private static void flatMapWithRetryExample() throws InterruptedException {

        Disposable disposable = repository.getDiskData()
                .flatMap((Function<String, ObservableSource<String>>) s -> {
                    out.println(s);
                    return repository.getNetworkData();
                })
                .flatMap(new Function<String, ObservableSource<?>>() {
                    @Override
                    public ObservableSource<?> apply(String s) throws Exception {
                        if (s.equalsIgnoreCase("SomeOtherData")) {
                            // problem, throw an error and the .retryWhen will catch it
                            return Observable.error(new IllegalArgumentException());
                        }
                        return Observable.just(s);
                    }
                })
                .retryWhen(new Function<Observable<Throwable>, ObservableSource<?>>() {
                    @Override
                    public ObservableSource<?> apply(Observable<Throwable> throwableObservable) throws Exception {
                        return throwableObservable.flatMap(new Function<Throwable, ObservableSource<?>>() {
                            @Override
                            public ObservableSource<?> apply(Throwable throwable) throws Exception {
                                if (throwable instanceof IllegalArgumentException) {
                                    out.println("Got it");
                                    return Observable.just(0); // retry, the int here is irrelevant
                                } else {
                                    // other error, pass it further
                                    return Observable.error(throwable);
                                }
                            }
                        });
                    }
                })
                .subscribe(out::println);

        Thread.sleep(3000);
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



    private static void publishAndConnect() throws InterruptedException {
        Observable<String> observable = repository.getUsersFirstName();
        Disposable dis = observable.subscribe(i -> out.println("first subscription : " + i));
        Observable<String> observable1 = repository.getUsersFirstName();
        Thread.sleep(100);
        observable1.subscribe(i -> out.println("second subscription : " + i));

        Thread.sleep(1000);
    }
}
