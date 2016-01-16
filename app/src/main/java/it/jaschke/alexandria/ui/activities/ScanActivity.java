package it.jaschke.alexandria.ui.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.Toolbar;

import butterknife.Bind;
import butterknife.ButterKnife;
import it.jaschke.alexandria.R;
import it.jaschke.alexandria.managers.BeepManager;
import it.jaschke.alexandria.ui.fragments.AddBookFragment;
import it.jaschke.alexandria.ui.fragments.ScanFragment;

public class ScanActivity extends BaseToolBarActivity implements ScanFragment.InterfaceScanCompleteCallBack {

    // Logging Identifier for class
    private final String LOG_TAG = ScanActivity.class.getSimpleName();
    // ButterKnife injected views
    // The surface view containing layout
    @Bind(R.id.transparent_toolbar)
    Toolbar mTransparentToolBar;
    // Media use
    private BeepManager mBeepManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan);
        // Inflate all views
        ButterKnife.bind(this);
        // Set the Toolbar
        setToolbar(mTransparentToolBar, true, false, 0);
        setToolbarTransparent(true);

        // Check that the activity is using the layout with the fragment_container id
        if (findViewById(R.id.fragment_container) != null) {
            // However, if we're being restored from a previous state,
            // then we don't need to do anything and should return or else
            // we could end up with overlapping fragments.
            if (savedInstanceState != null) {
                return;
            }
            // Create the detail fragment and add it to the activity
            // using a fragment transaction.
            else {
                // Pass bundle to the fragment
                showScanFragment(null);
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        // Initialize the beepmanager
        mBeepManager = new BeepManager(this);
    }

    // Called when the activity is no longer resumed
    @Override
    public void onPause() {
        super.onPause();
        if (mBeepManager != null) {
            mBeepManager.close();
            mBeepManager = null;
        }
    }

    // Called to destroy this fragment
    @Override
    public void onDestroy() {
        super.onDestroy();
        ButterKnife.unbind(this);
    }

    // Insert the DetailFragment
    private void showScanFragment(Bundle args) {
        // Create new scan fragment
        ScanFragment scanFragment = null;
        // Acquire the Fragment manger
        FragmentManager fm = getSupportFragmentManager();
        // Begin the transaction
        FragmentTransaction ft = fm.beginTransaction();
        // Check if we already have a fragment
        if (scanFragment == null) {
            // Create new fragment
            scanFragment = ScanFragment.newInstance(args);
            // Prefer replace() over add() see <a>https://github.com/RowlandOti/PopularMovies/issues/1</a>
            ft.replace(R.id.fragment_container, scanFragment);
            ft.commit();
        }
    }

    @Override
    public void onScanComplete(String scanResult) {
        mBeepManager.playBeepSoundAndVibrate();

        Bundle bundleScanResults = new Bundle();
        bundleScanResults.putString(AddBookFragment.REQ_SCAN_RESULTS, scanResult);
        // Send the bundles via the Intent
        Intent intentScanResults = new Intent();
        intentScanResults.putExtras(bundleScanResults);

        setResult(RESULT_OK, intentScanResults);
        finish();
    }
}
