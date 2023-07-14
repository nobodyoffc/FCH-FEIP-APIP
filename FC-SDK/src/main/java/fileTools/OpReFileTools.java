package fileTools;

import fchClass.OpReturn;
import javaTools.BytesTools;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;

import static constants.Constants.OPRETURN_FILE_NAME;
import static constants.Constants.OPRETURN_FILE_DIR;

public class OpReFileTools {

	public void writeOpReturnListIntoFile(ArrayList<OpReturn> opList) throws IOException  {

		if(opList==null || opList.isEmpty())return;
		String fileName = OPRETURN_FILE_NAME;
		File opFile;
		FileOutputStream opos;

			while(true) {
				opFile = new File(OPRETURN_FILE_DIR,fileName);
				if(opFile.length()>251658240) {
					fileName =  getNextFile(fileName);
				}else break;
			}
			if(opFile.exists()) {
				opos = new FileOutputStream(opFile,true);
			}else {
				opos = new FileOutputStream(opFile);
			}

		Iterator<OpReturn> iterOp = opList.iterator();
		while(iterOp.hasNext()) {
			ArrayList<byte[]> opArrList = new ArrayList<byte[]>();
			OpReturn op = iterOp.next();
			
			opArrList.add(BytesTools.intToByteArray(128+op.getOpReturn().getBytes().length));
			opArrList.add(BytesTools.hexToByteArray(op.getTxId()));
			opArrList.add(BytesTools.longToBytes(op.getHeight()));
			opArrList.add(BytesTools.longToBytes(op.getTime()));
			opArrList.add(BytesTools.intToByteArray(op.getTxIndex()));
			opArrList.add(op.getSigner().getBytes());
			if(op.getRecipient()==null || op.getRecipient().equals("nobody")) {
				opArrList.add("                                  ".getBytes());
			}else {
				opArrList.add(op.getRecipient().getBytes());
			}
			opArrList.add(BytesTools.longToBytes(op.getCdd()));
			opArrList.add(op.getOpReturn().getBytes());

				opos.write(BytesTools.bytesMerger(opArrList));
		}
		opos.flush();
		opos.close();
	}
	
	private static int getFileOrder(String currentFile) {	
		String s =String.copyValueOf(currentFile.toCharArray(), 8, 1);
		return Integer.parseInt(s);
	}

	private static String getFileNameWithOrder(int i) {
		return "opreturn"+String.format("%d",i)+".byte";
	}

	public static String getNextFile(String currentFile) {
		return getFileNameWithOrder(getFileOrder(currentFile)+1);
	}

}
