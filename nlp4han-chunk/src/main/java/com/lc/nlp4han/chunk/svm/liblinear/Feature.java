package com.lc.nlp4han.chunk.svm.liblinear;

/**
 * @since 1.9
 */
public interface Feature {

    int getIndex();

    double getValue();

    void setValue(double value);
}
