package com.example.ids.data.session;

import androidx.lifecycle.MutableLiveData;

public class SessionEventBus {

    // Emits true when session expires
    public static final MutableLiveData<Boolean> sessionExpired = new MutableLiveData<>();
}
