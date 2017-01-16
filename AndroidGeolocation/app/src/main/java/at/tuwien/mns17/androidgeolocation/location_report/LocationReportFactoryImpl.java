package at.tuwien.mns17.androidgeolocation.location_report;

import android.content.Context;
import android.telephony.CellInfo;
import android.telephony.TelephonyManager;
import android.util.Log;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;

import at.tuwien.mns17.androidgeolocation.service.MozillaLocationService;
import rx.Single;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class LocationReportFactoryImpl implements LocationReportFactory {

    private TelephonyManager telephonyManager;

    public LocationReportFactoryImpl(TelephonyManager telephonyManager) {
        this.telephonyManager = telephonyManager;
    }

    private static final String TAG = LocationReportRepositoryImpl.class.getName();

    @Override
    public Single<LocationReport> createLocationReport() {
        return Single.fromCallable(new LocationReportProducer(telephonyManager))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }

    private static class LocationReportProducer implements Callable<LocationReport> {

        private TelephonyManager telephonyManager;

        LocationReportProducer(TelephonyManager telephonyManager) {
            this.telephonyManager = telephonyManager;
        }

        @Override
        public LocationReport call() throws Exception {
            Log.d(TAG, "Creating new location report");

            LocationReport locationReport = new LocationReport();
            List<CellInfo> cells = getCellInfoList();
            Log.d(TAG, "Found " + cells.size() + " Cells");

            MozillaLocationService locationService = new MozillaLocationService();
            

            Log.d(TAG, "Location report created: " + locationReport);

            return locationReport;
        }

        private List<CellInfo> getCellInfoList() {
            List<CellInfo> allCellInfo = telephonyManager.getAllCellInfo();
            return allCellInfo == null ? Collections.emptyList() : allCellInfo;
        }
    }
}
