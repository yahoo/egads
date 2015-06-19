package com.yahoo.egads.data;

import java.util.Collection;

import com.yahoo.egads.data.Model.ModelType;

public interface ModelStore {
	public void storeModel(String tag, Model m);
	Model getModel(String tag, ModelType type);
	public void writeCachedModels();
	public Collection<Model> getCachedModels();
}
