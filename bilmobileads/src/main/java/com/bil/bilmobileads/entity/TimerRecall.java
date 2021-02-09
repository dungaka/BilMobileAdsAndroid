package com.bil.bilmobileads.entity;

import android.os.CountDownTimer;

import com.bil.bilmobileads.interfaces.WorkCompleteDelegate;

public class TimerRecall extends CountDownTimer {

    private WorkCompleteDelegate listener;

    public TimerRecall(long millisInFuture, long countDownInterval) {
        super(millisInFuture, countDownInterval);
    }

    public TimerRecall(long millisInFuture, long countDownInterval, WorkCompleteDelegate workCompleteDelegate) {
        super(millisInFuture, countDownInterval);

        this.listener = workCompleteDelegate;
    }


    public void onTick(long l) {
//        PBMobileAds.getInstance().log("onTick: " + l / 1000);
    }


    public void onFinish() {
        if (this.listener != null) {
            this.listener.doWork();
        }
    }

    public void setListener(WorkCompleteDelegate listener) {
        this.listener = listener;
    }
}