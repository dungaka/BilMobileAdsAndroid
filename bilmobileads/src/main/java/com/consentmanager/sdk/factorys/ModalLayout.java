package com.consentmanager.sdk.factorys;

import android.content.Context;
import android.widget.LinearLayout;

public class ModalLayout {
    private LinearLayout layout;
    private Context context;
    private static ModalLayout instance;

    private ModalLayout(Context context){
        this.context = context;
    }

    public static ModalLayout initialise(Context context){
        if( instance == null){
            instance = new ModalLayout(context);
        }
        return instance;
    }

    public LinearLayout getLayout(){
        if( layout == null){
            layout = this.createLayout();
        }
        return this.layout;
    }

    private LinearLayout createLayout(){

        // create the Layout
        LinearLayout linearLayout = new LinearLayout(context);

        //set the WebView to the Layout
        linearLayout.addView(WebViewCreator.initialise(context).getWebView());

        //Hide Layer
        linearLayout.setVisibility(LinearLayout.VISIBLE);

        return linearLayout;
    }
}
