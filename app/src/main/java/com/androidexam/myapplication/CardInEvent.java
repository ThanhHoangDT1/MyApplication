package com.androidexam.myapplication;

import java.util.Locale;

public class CardInEvent implements IEvent {
    private final String _cardId;
    private final String _location;
    private String _sessionId;

    private final static String cardIn = "1|c:%s|i:";
    private final static String cardInWithLocationAndSessionId = "1|c:%s|i:|L:%s|sid:%s";

    /**
     *
     * @param cardId The Card ID of the player that should be carded in to the Cardless Connect
     *               system.
     * @param location The Table and Seat Location of the player that should be carded in to the Cardless Connect
     *               system.
     * @param sessionId The Session ID of the current active session of the player that should be carded in to the Cardless Connect
     *               system.
     */
    public CardInEvent(String cardId, String location, String sessionId) {
        this._cardId = cardId;
        this._location = location != null ? location : "";
        this._sessionId = sessionId != null ? sessionId : "";
    }

    /**
     *
     * @param cardId The Card ID of the player that should be carded in to the Cardless Connect
     *               system.
     */
    public CardInEvent(String cardId) {
        this(cardId, null, null);
    }

    /**
     * @exclude
     */
    @Override
    public byte[] getBytes() {
        if (_location.isEmpty() && _sessionId.isEmpty()) {
            return String.format(Locale.getDefault(), cardIn, _cardId).getBytes();
        }
        return String.format(Locale.getDefault(), cardInWithLocationAndSessionId, _cardId, _location, _sessionId).getBytes();
    }

    @Override
    public String toString() {
        return String.format(Locale.getDefault(), "CardInEvent, cardId: %s, location: %s, sessionId: %s", _cardId, _location, _sessionId);
    }

    /**
     *
     * Set the Session ID.
     *
     * @param sessionId The Session ID of the current active session in the Cardless Connect system
     */
    @Override
    public void setSessionId(String sessionId) {
        this._sessionId = sessionId;
    }
}
