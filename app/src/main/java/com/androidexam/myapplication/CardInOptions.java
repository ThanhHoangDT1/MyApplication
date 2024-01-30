package com.androidexam.myapplication;

import android.widget.EditText;

public class CardInOptions extends BaseOptions {
    @Override
    public int getName() {
         return R.string.cardIn;
    }

    @Override
    public int getLayout() {
        return R.layout.card_in_event_options;
    }

    @Override
    public IEvent getEvent() {
        String cardId = ((EditText)view.findViewById(R.id.cardid)).getText().toString();

        String table = ((EditText)view.findViewById(R.id.table)).getText().toString();
        String seat = ((EditText)view.findViewById(R.id.seat)).getText().toString();
        String location = seat.trim().isEmpty() ? table : table + "-" + seat;
        location = table.trim().isEmpty() ? null : location;

        String sessionId = ((EditText)view.findViewById(R.id.sessionid)).getText().toString();
        sessionId = sessionId.trim().isEmpty() ? null : sessionId;
        String serviceId = ((EditText)view.findViewById(R.id.serviceid)).getText().toString();
        serviceId = serviceId.trim().isEmpty() ? null : serviceId;

        return new CardInEvent(cardId, location, sessionId);
    }
}
