package com.androidexam.myapplication;

import java.util.Locale;

public class CardOutEvent implements IEvent {
    private final String _cardId;
    private String _sessionId;

    private final static String cardOut = "1|c:%s|o:";
    private final static String cardOutWithSessionId = "1|c:%s|o:|sid:%s";

    /**
     *
     * @param cardId The Card ID of the player that should be carded out from the Cardless Connect
     *               system.
     * @param sessionId The Session ID of the current active session of the player that should be carded out from the Cardless Connect
     *               system.
     */
    public CardOutEvent(String cardId, String sessionId) {
        this._cardId = cardId;
        this._sessionId = sessionId != null ? sessionId : "";
    }

    /**
     *
     * @param cardId The Card ID of the player that should be carded out from the Cardless Connect
     *               system.
     */
    public CardOutEvent(String cardId) {
        this(cardId, null);
    }

    /**
     * @exclude
     */
    @Override
    public byte[] getBytes() {
        if (_sessionId.isEmpty()) {
            return String.format(Locale.getDefault(), cardOut, _cardId).getBytes();
        }
        return String.format(Locale.getDefault(), cardOutWithSessionId, _cardId, _sessionId).getBytes();
    }

    @Override
    public String toString() {
        return String.format(Locale.getDefault(), "CardOutEvent, cardId: %s, sessionId: %s", _cardId, _sessionId);
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
