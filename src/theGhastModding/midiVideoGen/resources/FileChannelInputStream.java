package theGhastModding.midiVideoGen.resources;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

public class FileChannelInputStream extends InputStream {
	
	private ByteBuffer b;
	private FileChannel chann;
	private byte[] buffer; //ByteBuffer functions have a large overhead, so it's a better Idea to copy the ByteBuffer's contents into a byte array
	private int pos;
	private FileInputStream fis;
	
	public FileChannelInputStream(File f) throws IOException {
		fis = new FileInputStream(f);
		this.chann = fis.getChannel();
		this.b = ByteBuffer.allocate(1024 * 1024 * 4);
		chann.read(b);
		b.flip();
		this.buffer = new byte[b.limit()];
		b.get(buffer, 0, b.limit());
		this.pos = 0;
	}
	
	@Override
	public int read() throws IOException {
		if(pos >= buffer.length) {
			if(available() <= 0) return -1;
			b.clear();
			chann.read(b);
			b.flip();
			if(buffer.length != b.limit()) buffer = new byte[b.limit()];
			b.get(buffer);
			pos = 0;
		}
		byte a = buffer[pos];
		pos++;
		return a & 0xFF;
	}
	
	@Override
	public void close() throws IOException {
		b.clear();
		chann.close();
		fis.close();
	}
	
	@Override
	public long skip(long s) throws IOException {
		for(long i = 0; i < s; i++) {
			if(available() <= 0) return i;
			read();
		}
		return s;
	}
	
	@Override
	public int available() throws IOException {
		if(chann.size() - chann.position() <= 0 && pos < buffer.length) {
			return buffer.length - pos;
		}
		return (int) ((chann.size() - chann.position()) > Integer.MAX_VALUE ? Integer.MAX_VALUE : (chann.size() - chann.position()));
	}
	
}