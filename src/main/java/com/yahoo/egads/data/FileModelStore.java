package com.yahoo.egads.data;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

public class FileModelStore implements ModelStore {
	String path;
	public FileModelStore (String path) {
		File dir = new File (path);
		dir.mkdirs();
		this.path = path;
	}

	@Override
	public void storeModel(String tag, Model m) {
		String filename = tag.replaceAll("[^\\w_-]", "_");
		String fqn = path + "/" + filename;
		try {
			ObjectOutputStream o = new ObjectOutputStream(new FileOutputStream (fqn));
			o.writeObject(m);
			o.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public Model retrieveModel(String tag) {
		String filename = tag.replaceAll("[^\\w_-]", "_");
		String fqn = path + "/" + filename;
		Model m = null;
		try {
			ObjectInputStream o = new ObjectInputStream(new FileInputStream(fqn));
			m =  (Model) o.readObject();
			o.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return m;
	}

}
