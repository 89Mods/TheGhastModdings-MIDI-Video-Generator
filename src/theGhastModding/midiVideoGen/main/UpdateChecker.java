package theGhastModding.midiVideoGen.main;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import javax.swing.JOptionPane;

public class UpdateChecker {
	
	public static String newVersion = "null";
	
	public static boolean checkForUpdates(){
		FileOutputStream fos;
        try {
        	URL website = new URL("https://www.dropbox.com/s/dfh3qsyct1ztziu/converterversion.dat?dl=1");
        	ReadableByteChannel rbc = Channels.newChannel(website.openStream());
        	fos = new FileOutputStream("version.dat");
        	fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
        	fos.close();
        }catch (Exception e2){
        	JOptionPane.showMessageDialog(MidiVideoGenMain.frame, "Error downloading version.dat for update check: " + e2.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        	e2.printStackTrace();
        	return false;
        }
		InputStream in;
		DataInputStream dis;
		newVersion = "";
		try {
			in = new FileInputStream("version.dat");
			dis = new DataInputStream(new GZIPInputStream(in));
			newVersion = dis.readUTF();
			dis.close();
		} catch (Exception e) {
			JOptionPane.showMessageDialog(MidiVideoGenMain.frame, "Error checking for updates: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
			e.printStackTrace();
			return false;
		}
		try {
			new File("version.dat").delete();
		}catch(Exception e){
			JOptionPane.showMessageDialog(MidiVideoGenMain.frame, "Error deleting version.dat after update check: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
			e.printStackTrace();
		}
		if(!newVersion.startsWith("2")) {
			//return false;
		}
		if(newVersion.contentEquals(MidiVideoGenMain.VERSION)){
			return false;
		}else{
			return true;
		}
	}
	
	public static void writeVersion(String version){
		File file = new File("converterversion.dat");
		if(!file.exists()){
			try {
				file.createNewFile();
			} catch (Exception e) {
				JOptionPane.showMessageDialog(MidiVideoGenMain.frame, "Error creating midiversion.dat: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
				e.printStackTrace();
			}
		}
		OutputStream out = null;
		try {
			out = new FileOutputStream(file);
		} catch (Exception e) {
			e.printStackTrace();
		}
		try {
			DataOutputStream dos = new DataOutputStream(new GZIPOutputStream(out));
			dos.writeUTF(version);
			dos.flush();
			dos.close();
		} catch (Exception e) {
			JOptionPane.showMessageDialog(MidiVideoGenMain.frame, "Error writing to midiversion.dat: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
			e.printStackTrace();
		}
		try {
			out.close();
		} catch (Exception e) {
			JOptionPane.showMessageDialog(MidiVideoGenMain.frame, "Error closing DataOutputStream: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
			e.printStackTrace();
		}
	}
	
}