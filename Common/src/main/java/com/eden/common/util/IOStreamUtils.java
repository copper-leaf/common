package com.eden.common.util;

import com.caseyjbrooks.clog.Clog;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.nio.charset.Charset;

public class IOStreamUtils {

    public static class InputStreamCollector extends BaseInputStreamHandler {

        private StringBuffer output;

        public InputStreamCollector(InputStream inputStream) {
            super(inputStream);
        }

        @Override
        protected void onBeforeRead() {
            output = new StringBuffer();
        }

        @Override
        protected void onReadLine(String line) {
            output.append(line + "\n");
        }

        @Override
        public String toString() {
            return output.toString();
        }
    }

    public static class InputStreamPrinter extends BaseInputStreamHandler {

        private final String tag;

        public InputStreamPrinter(InputStream inputStream, String tag) {
            super(inputStream);
            this.tag = tag;
        }

        @Override
        protected void onReadLine(String line) {
            if (tag != null) {
                Clog.tag(tag).log(line);
            }
            else {
                Clog.noTag().log(line);
            }
        }
    }

    public static class InputStreamIgnorer extends BaseInputStreamHandler {

        public InputStreamIgnorer(InputStream inputStream) {
            super(inputStream);
        }

        @Override
        protected void onReadLine(String line) {

        }
    }

    private static abstract class BaseInputStreamHandler implements Runnable {

        private final InputStream inputStream;

        protected BaseInputStreamHandler(InputStream inputStream) {
            if(inputStream == null) throw new NullPointerException("inputStream");
            this.inputStream = inputStream;
        }

        @Override
        public final void run() {
            onBeforeRead();
            final InputStreamReader inputStreamReader = new InputStreamReader(inputStream, Charset.forName("UTF-8"));

            try (BufferedReader bufferedReader = new BufferedReader(inputStreamReader)) {
                String line = bufferedReader.readLine();
                while (line != null) {
                    onReadLine(line);
                    line = bufferedReader.readLine();
                }
            }
            catch (Exception e) { }
            onAfterRead();
        }

        protected void onBeforeRead() {}
        protected abstract void onReadLine(String line);
        protected void onAfterRead() {}
    }

    public static InputStream converOutputToInputStream(final OnOutputStreamCreatedCallback callback) throws IOException {
        final PipedInputStream pipedInputStream = new PipedInputStream();
        final PipedOutputStream pipedOutputStream = new PipedOutputStream(pipedInputStream);

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    callback.onOutputStreamCreated(pipedOutputStream);
                }
                finally {
                    try {
                        pipedOutputStream.close();
                    }
                    catch (Exception e) {

                    }
                }
            }
        }).start();

        return pipedInputStream;
    }

    public interface OnOutputStreamCreatedCallback {
        void onOutputStreamCreated(OutputStream os);
    }

}
