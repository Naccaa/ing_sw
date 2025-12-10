package com.example.ids.ui.guide;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class GuideViewModel extends ViewModel {

    private final MutableLiveData<String> mText;

    public GuideViewModel() {
        mText = new MutableLiveData<>();
        mText.setValue("This is Guide fragment");
    }

    public LiveData<String> getText() {
        return mText;
    }
}