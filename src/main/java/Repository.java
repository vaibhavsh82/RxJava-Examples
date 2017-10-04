import io.reactivex.Observable;

import java.util.concurrent.TimeUnit;

class Repository {

    Observable<String> getUsersFirstName() {
        return Observable.fromArray("George", "Will", "Brad", "Amitabh")
                .zipWith(Observable.interval(500, TimeUnit.MILLISECONDS), (item, interval) -> item);
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
        return Observable.just("NetworkData")
                .map(s -> {
                    //int a = 9/0;
                    return s;
                })
                .zipWith(Observable.interval(300, TimeUnit.MILLISECONDS), (item, interval) -> item);
    }

    void saveDataToDisk(String s) {
        // save to the disk
    }
}
