package com.creditdatamw.labs.kapenta.io;

import java.io.IOException;
import java.io.OutputStream;

/**
 * <p>
 * MultiplexOutputStream alloes you to write to multiple output streams "at once".
 * It allows you to use one outputstream writer to write to multiple outputstreams
 * without repeating yourself.
 * </p>
 * <strong>Example:</strong>
 *
 * <pre>
 *   File file1 = new File("file1.java");
 *   File file2 = new File("file2.java");
 *
 *   try (FileOutputStream fos1 = new FileOutputStream(file1);
 *        FileOutputStream fos2 = new FileOutputStream(file2);
 *        MultiplexOutputStream mos = new MultiplexOutputStream(fos1, fos2)) {
 *
 *       Files.copy(Paths.get("MultiplexOutputStream.java"), mos);
 *
 *   } catch(IOException ex) {
 *       ex.printStackTrace();
 *   }
 * </pre>
 */
public class MultiplexOutputStream extends OutputStream {

    private OutputStream[] outputStreams;

    public MultiplexOutputStream(OutputStream... outputStreams) {
        java.util.Objects.requireNonNull(outputStreams);
        assert(outputStreams.length > 0);
        for(Object o: outputStreams) {
            java.util.Objects.requireNonNull(o);
        }
        this.outputStreams = outputStreams;
    }

    @Override
    public void write(int b) throws IOException {
        for(OutputStream os: outputStreams) {
            os.write(b);
        }
    }

    @Override
    public void write(byte[] b) throws IOException {
        for(OutputStream os: outputStreams) {
            os.write(b);
        }
    }

    @Override
    public void write(byte[] b, int off, int len) throws IOException {
        for(OutputStream os: outputStreams) {
            os.write(b, off, len);
        }
    }

    @Override
    public void flush() throws IOException {
        for(OutputStream os: outputStreams) {
            os.flush();
        }
    }

    @Override
    public void close() throws IOException {
        for(OutputStream os: outputStreams) {
            os.close();
        }
    }
}