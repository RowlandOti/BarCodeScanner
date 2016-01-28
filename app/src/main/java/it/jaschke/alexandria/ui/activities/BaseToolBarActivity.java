/*
 * Copyright 2015 Oti Rowland
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package it.jaschke.alexandria.ui.activities;

import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;

import com.facebook.stetho.Stetho;

/**
 * Created by Oti Rowland on 12/20/2015.
 */
public class BaseToolBarActivity extends AppCompatActivity {

    // Class Variables
    private final String LOG_TAG = BaseToolBarActivity.class.getSimpleName();
    // The toolbar
    protected Toolbar mToolbar;

    // Should we show master-detail layout?
    protected boolean mIsTwoPane;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    // Derived classes have access to this method
    protected void setToolbar(boolean showHomeUp, boolean showTitle, int iconResource) {
        setToolbar(mToolbar, showHomeUp, showTitle, iconResource);
    }

    // Derived methods have no direct access to this class
    public void setToolbar(Toolbar mToolbar, boolean isShowHomeUp, boolean isShowTitle, int iconResource) {
        // Does the inc_toolbar exist?
        if (mToolbar != null) {
            setSupportActionBar(mToolbar);
            // Should we set up home-up button navigation?
            getSupportActionBar().setDisplayHomeAsUpEnabled(isShowHomeUp);
            // Should we display the title on the inc_toolbar?
            getSupportActionBar().setDisplayShowTitleEnabled(isShowTitle);
            // Should we set logo to appear in inc_toolbar?
            if (!(iconResource == 0)) {
                getSupportActionBar().setIcon(iconResource);
                //this.mToolbar.setLogo(R.drawable.ic_logo_48px);
            }
        }
    }

    public void setToolbarTransparent(boolean isToolbarTransparent) {
        if (isToolbarTransparent) {
            // Check for minimum api as Lollipop
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                // Set up  the systemUi flags
                getWindow().getDecorView().setSystemUiVisibility(
                        View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
                // Set the status bar tobe transparent
                getWindow().setStatusBarColor(Color.TRANSPARENT);
            }
        }
    }

    // Network monitoring using facebook's lethal Stetho
    // ToDo: Remove this method, its just for debuging
    protected void initStetho() {
        Stetho.initialize(
                Stetho.newInitializerBuilder(this)
                        .enableDumpapp(Stetho.defaultDumperPluginsProvider(this))
                        .enableWebKitInspector(Stetho.defaultInspectorModulesProvider(this))
                        .build());
    }

    // Should we show master-detail layout?
    protected void toggleShowTwoPane(boolean isShowTwoPane) {
        mIsTwoPane = isShowTwoPane;
    }

    // Are we a master-detail layout?
    public boolean getIsTwoPane() {
        return mIsTwoPane;
    }

}
