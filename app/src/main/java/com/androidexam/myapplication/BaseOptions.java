package com.androidexam.myapplication;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

abstract class BaseOptions {
    protected View view = null;

    abstract int getName();

    abstract int getLayout();

    void bind(ViewGroup optionsView, LayoutInflater layoutInflater) {
        optionsView.removeAllViews();
        if (view == null) {
            this.view = layoutInflater.inflate(getLayout(), null);
        }
        optionsView.addView(this.view);
    }

    abstract IEvent getEvent();

    private EditText getField(int id) {
        if (view == null) {
            return null;
        }
        return (EditText)view.findViewById(id);
    }

    String getFieldValue(int id) {
        EditText field = getField(id);
        if (field != null) {
            return field.getText().toString();
        }
        return null;
    }

    void setFieldValue(int id, String value) {
        EditText field = getField(id);
        if (field != null) {
            field.setText(value);
        }
    }
}
