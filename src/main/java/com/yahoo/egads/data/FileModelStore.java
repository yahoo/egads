package com.yahoo.egads.data;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.HashMap;

public class FileModelStore implements ModelStore {
	HashMap <String, Model> cache;
	String path;
    protected static org.apache.logging.log4j.Logger logger = org.apache.logging.log4j.LogManager.getLogger(FileModelStore.class.getName());

	public FileModelStore (String path) {
		File dir = new File (path);
		dir.mkdirs();
		this.path = path;
		cache = new HashMap<String, Model>();
	}

	@Override
	public void storeModel(String tag, Model m) {
		String filename = tag.replaceAll("[^\\w_-]", "_");
		String fqn = path + "/" + filename;
		try {
			m.clearModified();
			ObjectOutputStream o = new ObjectOutputStream(new FileOutputStream (fqn));
			o.writeObject(m);
			o.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public Model getModel(String tag) {
		String filename = tag.replaceAll("[^\\w_-]", "_");
		if (cache.containsKey(filename)) {
			return cache.get(filename);
		}
		String fqn = path + "/" + filename;
		Model m = null;
		try {
			ObjectInputStream o = new ObjectInputStream(new FileInputStream(fqn));
			m =  (Model) o.readObject();
			o.close();
			cache.put(filename, m);
			return m;
		} catch (Exception e) {
			logger.debug("Model not found: " + tag);
		}
		return null;
	}
	public void writeCachedModels() {
		for (String key : cache.keySet()) {
			Model model = cache.get(key);
			if (model.isModified()) {
				storeModel(key, model);
			}
		}
	}

}
