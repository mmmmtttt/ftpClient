package com.ss.ftpClient;

import org.junit.Test;

import com.ss.ftpClient.enums.Mode;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class ExampleUnitTest {
    @Test
    public void test() {
        Mode mode = Mode.STREAM;
        System.out.println(mode);
    }
}