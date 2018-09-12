/*
 JsonStringifyPrettyPrinter.java
 Copyright (c) 2018 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.codegen.util;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.PrettyPrinter;

import java.io.IOException;
import java.util.Arrays;
import java.util.Stack;

/**
 * Google Chrome における JSON.stringify() に近い pretty print を実現します。
 * <p>
 * このオブジェクトはスレッドセーフではありません。
 * </p>
 *
 * @author KOMIYA Atsushi
 */
public class JsonStringifyPrettyPrinter implements PrettyPrinter {
    enum State {
        ROOT_VALUE_SEPARATOR,
        START_OBJECT,
        END_OBJECT,
        OBJECT_ENTRY_SEPARATOR,
        OBJECT_FIELD_VALUES_SEPARATOR,
        START_ARRAY,
        END_ARRAY,
        ARRAY_VALUE_SEPARATOR,
        BEFORE_ARRAY_VALUES,
        BEFORE_OBJECT_ENTRIES
    }

    private static final String LINE_SEPARATOR = System.lineSeparator();
    private static final char[] SPACES = new char[128];

    static {
        Arrays.fill(SPACES, ' ');
    }

    private final int numSpacesPerIndent;
    private int indentLevel;
    private State lastState;
    private Stack<Boolean> listContainsLiteralOnly = new Stack<>();

    /**
     * インデントのときのスペース個数を指定して、JsonStringifyPrettyPrinter オブジェクトを生成します。
     *
     * @param numSpacesPerIndent インデントのときのスペース個数を指定します
     */
    public JsonStringifyPrettyPrinter(int numSpacesPerIndent) {
        this.numSpacesPerIndent = numSpacesPerIndent;
    }

    private void indent(JsonGenerator jg) throws IOException {
        jg.writeRaw(LINE_SEPARATOR);

        int numSpacesToBeOutput = indentLevel * numSpacesPerIndent;
        while (numSpacesToBeOutput > SPACES.length) {
            jg.writeRaw(SPACES, 0, SPACES.length);
            numSpacesToBeOutput -= SPACES.length;
        }

        jg.writeRaw(SPACES, 0, numSpacesToBeOutput);
    }

    @Override
    public void writeRootValueSeparator(JsonGenerator jg) throws IOException {
        lastState = State.ROOT_VALUE_SEPARATOR;
    }

    @Override
    public void writeStartObject(JsonGenerator jg) throws IOException {
        if (lastState != State.OBJECT_FIELD_VALUES_SEPARATOR) {
            indent(jg);
        }
        jg.writeRaw("{");
        indentLevel++;

        if (!listContainsLiteralOnly.empty() && listContainsLiteralOnly.peek()) {
            listContainsLiteralOnly.pop();
            listContainsLiteralOnly.push(false);
        }

        lastState = State.START_OBJECT;
    }

    @Override
    public void writeEndObject(JsonGenerator jg, int nrOfEntries) throws IOException {
        indentLevel--;
        indent(jg);
        jg.writeRaw("}");

        lastState = State.END_OBJECT;
    }

    @Override
    public void writeObjectEntrySeparator(JsonGenerator jg) throws IOException {
        jg.writeRaw(",");
        indent(jg);

        lastState = State.OBJECT_ENTRY_SEPARATOR;
    }

    @Override
    public void writeObjectFieldValueSeparator(JsonGenerator jg) throws IOException {
        jg.writeRaw(": ");

        lastState = State.OBJECT_FIELD_VALUES_SEPARATOR;
    }

    @Override
    public void writeStartArray(JsonGenerator jg) throws IOException {
        if (lastState != State.OBJECT_FIELD_VALUES_SEPARATOR) {
            indent(jg);
        }
        jg.writeRaw("[");
        indentLevel++;

        listContainsLiteralOnly.push(true);

        lastState = State.START_ARRAY;
    }

    @Override
    public void writeEndArray(JsonGenerator jg, int nrOfValues) throws IOException {
        indentLevel--;

        if (!listContainsLiteralOnly.pop()) {
            indent(jg);
        }
        jg.writeRaw("]");

        lastState = State.END_ARRAY;
    }

    @Override
    public void writeArrayValueSeparator(JsonGenerator jg) throws IOException {
        jg.writeRaw(", ");

        lastState = State.ARRAY_VALUE_SEPARATOR;
    }

    @Override
    public void beforeArrayValues(JsonGenerator jg) throws IOException {
        lastState = State.BEFORE_ARRAY_VALUES;
    }

    @Override
    public void beforeObjectEntries(JsonGenerator jg) throws IOException {
        indent(jg);
        lastState = State.BEFORE_OBJECT_ENTRIES;
    }
}