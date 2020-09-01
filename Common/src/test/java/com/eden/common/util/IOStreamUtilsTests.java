package com.eden.common.util;

import clog.Clog;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.PrintWriter;
import java.nio.charset.Charset;

import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;

public class IOStreamUtilsTests {

    @Test
    public void testInputStreamCollector() throws Throwable {
        InputStream inputStream = new ByteArrayInputStream("Hello, world!".getBytes(Charset.forName("UTF-8")));
        IOStreamUtils.InputStreamCollector handler = new IOStreamUtils.InputStreamCollector(inputStream);
        handler.run();

        assertThat(handler.toString(), is(equalTo("Hello, world!\n")));
    }

    @Test
    public void testInputStreamPrinterNoTag() throws Throwable {
        InputStream inputStream = new ByteArrayInputStream("Hello, world!".getBytes(Charset.forName("UTF-8")));
        IOStreamUtils.InputStreamPrinter handler = new IOStreamUtils.InputStreamPrinter(inputStream, null);
        handler.run();

//        assertThat(Clog.getInstance().getLastTag(), is(equalTo("")));
//        assertThat(Clog.getInstance().getLastLog(), is(equalTo("Hello, world!")));
    }

    @Test
    public void testInputStreamPrinterWithTag() throws Throwable {
        InputStream inputStream = new ByteArrayInputStream("Hello, world!".getBytes(Charset.forName("UTF-8")));
        IOStreamUtils.InputStreamPrinter handler = new IOStreamUtils.InputStreamPrinter(inputStream, "test");
        handler.run();

//        assertThat(Clog.getInstance().getLastTag(), is(equalTo("test")));
//        assertThat(Clog.getInstance().getLastLog(), is(equalTo("Hello, world!")));
    }

    @Test
    public void testInputStreamIgnorer() throws Throwable {
        InputStream inputStream = new ByteArrayInputStream("Hello, world!".getBytes(Charset.forName("UTF-8")));
        IOStreamUtils.InputStreamIgnorer handler = new IOStreamUtils.InputStreamIgnorer(inputStream);
        handler.run();

//        assertThat(Clog.getInstance().getLastTag(), is(equalTo(null)));
//        assertThat(Clog.getInstance().getLastLog(), is(equalTo(null)));
    }

    @Test
    public void testConvertOutputStreamToInputStream() throws Throwable {
        InputStream inputStream = IOStreamUtils.converOutputToInputStream(os -> {
            PrintWriter writer = new PrintWriter(os);
            writer.write("Hello, world!");
            writer.flush();
            writer.close();
        });

        IOStreamUtils.InputStreamCollector handler = new IOStreamUtils.InputStreamCollector(inputStream);
        handler.run();

        Thread.sleep(250);

        assertThat(handler.toString(), is(equalTo("Hello, world!\n")));
    }

}
