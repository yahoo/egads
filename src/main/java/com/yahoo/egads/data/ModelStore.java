package com.yahoo.egads.data;

public interface ModelStore {
	public void storeModel(String tag, Model m);
	public Model getModel (String tag);
	public void writeCachedModels();
}
