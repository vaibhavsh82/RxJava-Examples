import io.reactivex.Observable;

import java.util.concurrent.TimeUnit;

class Repository {

    Observable<String> stringObservable;
    private CachedRequest<String> obs;

    Observable<String> getUsersFirstName() {
        if (stringObservable == null)
        stringObservable = Observable.fromArray("George", "Will", "Brad", "Amitabh")
                .zipWith(Observable.interval(10, TimeUnit.MILLISECONDS), (item, interval) -> item);
        obs = new CachedRequest(stringObservable, 50, TimeUnit.MILLISECONDS);
        return obs.getCachedObservable();
    }

    Observable<String> getUsersLastName() {
        return Observable.fromArray("Clooney", "Smith", "Pitt", "Bachchan")
                .zipWith(Observable.interval(100, TimeUnit.MILLISECONDS), (item, interval) -> item);
    }


    Observable<String> getDiskData() {
        return Observable.just("DiskData")
                .zipWith(Observable.interval(100, TimeUnit.MILLISECONDS), (item, interval) -> item);
    }

    Observable<String> getNetworkData() {
        return Observable.fromArray("SomeOtherData", "NetworkData")
                .map(s -> {
                    //int a = 9/0;
                    return s;
                })
                .zipWith(Observable.interval(1000, TimeUnit.MILLISECONDS), (item, interval) -> item);
    }

    void saveDataToDisk(String s) {
        // save to the disk
        System.out.println(s);
    }
}
