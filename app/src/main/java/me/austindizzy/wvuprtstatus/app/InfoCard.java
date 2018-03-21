package me.austindizzy.wvuprtstatus.app;

import android.content.Context;
import android.support.v7.widget.CardView;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.TextView;

public class InfoCard extends CardView {
    public InfoCard(Context context) {
        super(context);
        initialize(context);
    }

    public InfoCard(Context context, AttributeSet attrs) {
        super(context, attrs);
        initialize(context);
    }

    private void initialize(Context context) {
        LayoutInflater.from(context).inflate(R.layout.list_home, this);
        TextView mTextView = findViewById(R.id.info_card_text);
    }
}
