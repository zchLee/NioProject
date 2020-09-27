import org.junit.Test;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.Pipe;

/**
 * 通道：单向数据传输
 *
 * @author lzc
 * @create 2020.09.27 16:31
 */
public class PipeTest {

    @Test
    public void test() throws IOException {
        Pipe pipe = Pipe.open();

        ByteBuffer buffer = ByteBuffer.allocate(1024);
        // 获取通道
        Pipe.SinkChannel sinkChannel = pipe.sink();
        buffer.put("中文".getBytes());
        buffer.flip();
        sinkChannel.write(buffer);

        // 读取缓冲区数据
        Pipe.SourceChannel sourceChannel = pipe.source();
        buffer.flip();
        int len = sourceChannel.read(buffer);
        System.out.println(new String(buffer.array(), 0, len));

        sourceChannel.close();
        sinkChannel.close();
    }
}
