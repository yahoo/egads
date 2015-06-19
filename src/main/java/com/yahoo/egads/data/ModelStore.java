package com.yahoo.egads.data;

import java.util.Collection;

public interface ModelStore {
	public void storeModel(String tag, Model m);
	public Model getModel (String tag);
	public void writeCachedModels();
	public Collection<Model> getCachedModels();
}
