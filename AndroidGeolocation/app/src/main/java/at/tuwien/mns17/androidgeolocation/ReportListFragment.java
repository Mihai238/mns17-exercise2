package at.tuwien.mns17.androidgeolocation;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.telephony.TelephonyManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import at.tuwien.mns17.androidgeolocation.location_report.LocationReport;
import at.tuwien.mns17.androidgeolocation.location_report.LocationReportEmailSender;
import at.tuwien.mns17.androidgeolocation.location_report.LocationReportEmailSenderImpl;
import at.tuwien.mns17.androidgeolocation.location_report.LocationReportFactory;
import at.tuwien.mns17.androidgeolocation.location_report.LocationReportFactoryImpl;
import at.tuwien.mns17.androidgeolocation.location_report.LocationReportRepository;
import at.tuwien.mns17.androidgeolocation.location_report.LocationReportRepositoryImpl;
import rx.functions.Action1;

public class ReportListFragment extends Fragment {

    private LocationReportSelectionListener locationReportSelectionListener;

    private LocationReportFactory locationReportFactory;

    private LocationReportEmailSender locationReportEmailSender = new LocationReportEmailSenderImpl();

    private LocationReportRepository locationReportRepository = new LocationReportRepositoryImpl();

    private LocationReportsAdapter locationReportsAdapter;

    public static ReportListFragment newInstance() {
        return new ReportListFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_location_report_list, container, false);

        Toolbar toolbar = (Toolbar) view.findViewById(R.id.toolbar);
        ((AppCompatActivity) getActivity()).setSupportActionBar(toolbar);
        toolbar.setTitle(getActivity().getTitle());

        locationReportFactory = new LocationReportFactoryImpl((TelephonyManager) getActivity().getSystemService(Context.TELEPHONY_SERVICE), (WifiManager) getActivity().getSystemService(Context.WIFI_SERVICE), (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE));

        RecyclerView reportsRecyclerView = (RecyclerView) view.findViewById(R.id.location_reports_recycler_view);
        reportsRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        locationReportsAdapter = new LocationReportsAdapter(getContext(), locationReportRepository, locationReportEmailSender, locationReportSelectionListener);
        reportsRecyclerView.setAdapter(locationReportsAdapter);

        FloatingActionButton fab = (FloatingActionButton) view.findViewById(R.id.fab);
        fab.setOnClickListener(new CreateReportListener());

        return view;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof LocationReportSelectionListener) {
            locationReportSelectionListener = (LocationReportSelectionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement LocationReportSelectionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        locationReportSelectionListener = null;
    }

    private class CreateReportListener implements View.OnClickListener {

        @Override
        public void onClick(final View view) {
            locationReportFactory.createLocationReport()
                    .subscribe(report -> {
                        locationReportRepository.save(report);

                        Snackbar.make(view, "Location report created", Snackbar.LENGTH_LONG).show();

                        locationReportsAdapter.refresh();
                    });
        }
    }
}
