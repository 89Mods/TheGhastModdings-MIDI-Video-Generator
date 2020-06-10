package theGhastModding.midiVideoGen.resources;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

public class FileChannelOutputStream extends OutputStream {
	
	private FileChannel chan;
	private ByteBuffer b;
	private byte[] buffer;
	private int pos = 0;
	private FileOutputStream fos;
	
	public FileChannelOutputStream(File f) throws IOException {
		fos = new FileOutputStream(f);
		this.chan = fos.getChannel();
		this.b = ByteBuffer.allocate(1024 * 1024 * 4);
		this.buffer = new byte[1024 * 1024 * 4];
	}
	
	@Override
	public void write(int b) throws IOException {
		buffer[pos] = (byte)b;
		pos++;
		if(pos >= buffer.length) {
			flush();
		}
	}
	
	@Override
	public void close() throws IOException {
		flush();
		chan.close();
		fos.close();
	}
	
	@Override
	public void flush() throws IOException {
		this.b.put(buffer, 0, pos);
		pos = 0;
		this.b.flip();
		chan.write(this.b);
		this.b.clear();
	}
	
}