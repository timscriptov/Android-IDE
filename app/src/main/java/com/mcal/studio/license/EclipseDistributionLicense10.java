package com.mcal.studio.license;

import android.content.Context;

import com.mcal.studio.R;

import de.psdev.licensesdialog.licenses.License;

public class EclipseDistributionLicense10 extends License {

    @Override
    public String getName() {
        return "Eclipse Distribution License 1.0";
    }

    @Override
    public String readSummaryTextFromResources(Context context) {
        return getContent(context, R.raw.edl_v10);
    }

    @Override
    public String readFullTextFromResources(Context context) {
        return getContent(context, R.raw.edl_v10);
    }

    @Override
    public String getVersion() {
        return "1.0";
    }

    @Override
    public String getUrl() {
        return "http://www.eclipse.org/org/documents/edl-v10.php";
    }
}
