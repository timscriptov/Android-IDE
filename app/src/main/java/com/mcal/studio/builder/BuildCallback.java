package com.mcal.studio.builder;

import java.io.File;

public interface BuildCallback {

    public void onStart();

    public void onFailure(String message);

    public void onProgress(String progress);

    public void onSuccess(File apk);
}