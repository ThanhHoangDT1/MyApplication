package com.androidexam.myapplication;

import android.widget.EditText;

public class CardOutOptions extends BaseOptions {
    @Override
    public int getName() {
        return R.string.cardOut;
    }

    @Override
    public int getLayout() {
        return R.layout.card_in_event_options;
    }

    @Override
    public IEvent getEvent() {
        String cardId = ((EditText)view.findViewById(R.id.cardid)).getText().toString();
        String sessionId = ((EditText)view.findViewById(R.id.sessionid)).getText().toString();
        sessionId = sessionId.trim().isEmpty() ? null : sessionId;
        return new CardOutEvent(cardId, sessionId);
    }
}
