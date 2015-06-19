package com.yahoo.egads.data;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Collection;
import java.util.HashMap;

public class FileModelStore implements ModelStore {
	HashMap <String, Model> cache;
	String path;
    protected static org.apache.logging.log4j.Logger logger = org.apache.logging.log4j.LogManager.getLogger(FileModelStore.class.getName());

	public FileModelStore (String path) {
		this.path = path;
		cache = new HashMap<String, Model>();
		new File (path).mkdirs();
	}
	
	private String getFilename (String tag, Model.ModelType type) {
		String filename = tag.replaceAll("[^\\w_-]", "_");
		if (type == Model.ModelType.ANOMALY) {
			filename = "anomaly." + filename;
		} else if (type == Model.ModelType.FORECAST) {
			filename =  "forecast." + filename;
		}
		return filename;
	}

	@Override
	public void storeModel(String tag, Model m) {
		String filename = getFilename(tag, m.getModelType());
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
	public Model getModel(String tag, Model.ModelType type) {
		String filename = getFilename(tag, type);
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
//				The key always has the model type prepended - remove it before storing
				key = key.replaceFirst("[a-zA-Z]*\\.", "");
				storeModel(key, model);
			}
		}
	}
	public Collection<Model> getCachedModels() {
		return cache.values();
	}

}
